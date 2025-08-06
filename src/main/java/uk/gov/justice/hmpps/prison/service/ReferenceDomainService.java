package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCodeInfo;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.ReferenceDataRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceDomainRepository;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;
import uk.gov.justice.hmpps.prison.service.support.StringWithAbbreviationsProcessor;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReferenceDomainService {
    private final ReferenceDataRepository referenceDataRepository;
    private final ReferenceDomainRepository referenceDomainRepository;
    private final ProfileCodeRepository profileCodeRepository;
    private final ProfileTypeRepository profileTypeRepository;

    public ReferenceDomainService(
        final ReferenceDataRepository referenceDataRepository,
        ReferenceDomainRepository referenceDomainRepository,
        ProfileCodeRepository profileCodeRepository,
        ProfileTypeRepository profileTypeRepository
    ) {
        this.referenceDataRepository = referenceDataRepository;
        this.referenceDomainRepository = referenceDomainRepository;
        this.profileCodeRepository = profileCodeRepository;
        this.profileTypeRepository = profileTypeRepository;
    }

    private static String getDefaultOrderBy(final String orderBy) {
        return StringUtils.defaultIfBlank(orderBy, "code");
    }

    private static Order getDefaultOrder(final Order order) {
        return Objects.isNull(order) ? Order.ASC : order;
    }

    @Transactional
    public ReferenceCode createReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceData) {
        referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false).ifPresent(p -> {
            throw new EntityAlreadyExistsException(domain + "/" + code);
        });

        final var data = referenceData.toBuilder()
                .activeFlag(referenceData.getActiveFlag() == null ? "Y" : referenceData.getActiveFlag())
                .systemDataFlag(referenceData.getSystemDataFlag() == null ? "Y" : referenceData.getSystemDataFlag())
                .listSeq(referenceData.getListSeq() == null ? 1 : referenceData.getListSeq())
                .build();

        if ("Y".equalsIgnoreCase(data.getActiveFlag())) {
            data.setExpiredDate(null);
        }
        if (referenceData.getExpiredDate() == null && "N".equalsIgnoreCase(data.getActiveFlag())) {
            data.setExpiredDate(LocalDate.now());
        }

        if (data.getParentCode() != null || data.getParentDomain() != null) {
            referenceDataRepository.getReferenceCodeByDomainAndCode(data.getParentDomain(), data.getParentCode(), false).orElseThrow(new EntityNotFoundException(data.getParentDomain() + "/" + data.getParentCode()));
        }

        referenceDataRepository.insertReferenceCode(domain, code, data);

        return referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false)
                .orElseThrow(new EntityNotFoundException(domain + "/" + code));
    }

    @Transactional
    public ReferenceCode updateReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceData) {

        final var previousRef = referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false).orElseThrow(new EntityNotFoundException(domain + "/" + code));

        final var data = referenceData.toBuilder()
                .activeFlag(referenceData.getActiveFlag() == null ? previousRef.getActiveFlag() : referenceData.getActiveFlag())
                .systemDataFlag(referenceData.getSystemDataFlag() == null ? previousRef.getSystemDataFlag() : referenceData.getSystemDataFlag())
                .listSeq(referenceData.getListSeq() == null ? previousRef.getListSeq() : referenceData.getListSeq())
                .expiredDate(referenceData.getExpiredDate() == null ? previousRef.getExpiredDate() : referenceData.getExpiredDate())
                .build();

        if ("Y".equalsIgnoreCase(data.getActiveFlag())) {
            data.setExpiredDate(null);
        }

        if (data.getExpiredDate() == null && "N".equalsIgnoreCase(data.getActiveFlag()) && previousRef.getExpiredDate() == null) {
            data.setExpiredDate(LocalDate.now());
        }

        if (data.getParentCode() != null || data.getParentDomain() != null) {
            referenceDataRepository.getReferenceCodeByDomainAndCode(data.getParentDomain(), data.getParentCode(), false).orElseThrow(new EntityNotFoundException(data.getParentDomain() + "/" + data.getParentCode()));
        }

        referenceDataRepository.updateReferenceCode(domain, code, data);

        return referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false)
                .orElseThrow(new EntityNotFoundException(domain + "/" + code));
    }

    public Page<ReferenceCode> getReferenceCodesByDomain(final String domain, final boolean withSubCodes, final String orderBy, final Order order, final long offset, final long limit) {
        verifyReferenceDomain(domain);

        return referenceDataRepository.getReferenceCodesByDomain(
                domain, withSubCodes, getDefaultOrderBy(orderBy), getDefaultOrder(order), offset, limit);
    }

    public List<ReferenceCode> getReferenceCodesByDomain(final String domain) {
        verifyReferenceDomain(domain);

        return referenceDataRepository.getReferenceCodesByDomain(domain, "code", Order.ASC);
    }

    public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(final String domain, final String code, final boolean withSubCodes) {
        verifyReferenceDomain(domain);
        verifyReferenceCode(domain, code);

        return referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, withSubCodes);
    }

    public List<ReferenceCode> getReferenceOrProfileCodesByDomain(final String domain) {
        if (StringUtils.isBlank(domain)) throw new EntityNotFoundException("Reference domain not specified");
        boolean domainExistsInReferenceCodes = referenceDataRepository.getReferenceDomain(domain).isPresent();

        if(domainExistsInReferenceCodes) {
            return referenceDataRepository.getReferenceCodesByDomain(domain, "code", Order.ASC);
        } else {
            var profileType = profileTypeRepository.findById(domain)
                .orElseThrow(EntityNotFoundException.withMessage("Reference domain [%s] not found.", domain));
            return getProfileCodesAsReferenceDataByType(profileType);
        }
    }

    private void verifyReferenceDomain(final String domain) {
        if (StringUtils.isBlank(domain)) throw new EntityNotFoundException("Reference domain not specified");
        referenceDataRepository.getReferenceDomain(domain)
                .orElseThrow(EntityNotFoundException.withMessage("Reference domain [%s] not found.", domain));
    }

    private void verifyReferenceCode(final String domain, final String code) {
        if (StringUtils.isBlank(code)) throw new EntityNotFoundException("Reference code not specified");
        referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false)
                .orElseThrow(EntityNotFoundException.withMessage("Reference code for domain [%s] and code [%s] not found.", domain, code));
    }

    private List<ReferenceCode> getProfileCodesAsReferenceDataByType(ProfileType type) {
        return profileCodeRepository.findByProfileType(type)
            .stream()
            .map(p -> ReferenceCode.builder()
                .domain(p.getId().getType().getType())
                .code(p.getId().getCode())
                .description(StringWithAbbreviationsProcessor.format(p.getDescription()))
                .listSeq(p.getListSequence())
                .activeFlag(p.isActive() ? "Y" : "N")
                .build())
            .sorted(Comparator.comparing(ReferenceCode::getCode))
            .collect(Collectors.toList());
    }

    public List<ReferenceCode> getScheduleReasons(final String eventType) {
        verifyReferenceCode(ReferenceDomain.INTERNAL_SCHEDULE_TYPE.getDomain(), eventType);

        final var scheduleReasons = referenceDataRepository.getScheduleReasons(eventType);
        return tidyDescriptionAndSort(scheduleReasons);
    }

    public boolean isReferenceCodeActive(final String domain, final String code) {
        // Call the advised version of the repository so that cacheing is applied.
        return referenceDataRepository
                .getReferenceCodeByDomainAndCode(domain, code, false)
                .map(rc -> "Y".equals(rc.getActiveFlag()))
                .orElse(false);
    }

    private List<ReferenceCode> tidyDescriptionAndSort(final List<ReferenceCode> refCodes) {
        return refCodes.stream()
                .map(p -> ReferenceCode.builder()
                        .code(p.getCode())
                        .description(StringWithAbbreviationsProcessor.format(p.getDescription()))
                        .build())
                .sorted(Comparator.comparing(ReferenceCode::getDescription))
                .collect(Collectors.toList());
    }

    public List<uk.gov.justice.hmpps.prison.api.model.ReferenceDomain> getAllDomains() {
        return referenceDomainRepository
            .findAll()
            .stream()
            .map(it -> uk.gov.justice.hmpps.prison.api.model.ReferenceDomain
                .builder()
                .domain(it.getDomain())
                .description(it.getDescription())
                .domainStatus(it.getDomainStatus())
                .ownerCode(it.getOwnerCode())
                .applnCode(it.getApplnCode())
                .parentDomain(it.getParentDomain())
                .build()
            )
            .sorted(Comparator.comparing(uk.gov.justice.hmpps.prison.api.model.ReferenceDomain::getDomain))
            .toList();
    }
}
