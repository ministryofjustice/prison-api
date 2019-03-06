package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.ReferenceCodeRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.ReferenceDomain;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReferenceDomainServiceImpl implements ReferenceDomainService {
    private final ReferenceCodeRepository referenceCodeRepository;

    public ReferenceDomainServiceImpl(final ReferenceCodeRepository referenceCodeRepository) {
        this.referenceCodeRepository = referenceCodeRepository;
    }

    private static String getDefaultOrderBy(final String orderBy) {
        return StringUtils.defaultIfBlank(orderBy, "code");
    }

    private static Order getDefaultOrder(final Order order) {
        return Objects.isNull(order) ? Order.ASC : order;
    }

    @Override
    public Page<ReferenceCode> getAlertTypes(final String orderBy, final Order order, final long offset, final long limit) {
        return referenceCodeRepository.getReferenceCodesByDomain(
                ReferenceDomain.ALERT.getDomain(), true,
                getDefaultOrderBy(orderBy), getDefaultOrder(order),
                offset, limit);
    }

    @Override
    public Page<ReferenceCode> getCaseNoteSources(final String orderBy, final Order order, final long offset, final long limit) {
        return referenceCodeRepository.getReferenceCodesByDomain(
                ReferenceDomain.CASE_NOTE_SOURCE.getDomain(), false,
                getDefaultOrderBy(orderBy), getDefaultOrder(order),
                offset, limit);
    }

    @Override
    public Page<ReferenceCode> getReferenceCodesByDomain(final String domain, final boolean withSubCodes, final String orderBy, final Order order, final long offset, final long limit) {
        verifyReferenceDomain(domain);

        return referenceCodeRepository.getReferenceCodesByDomain(
                domain, withSubCodes, getDefaultOrderBy(orderBy), getDefaultOrder(order), offset, limit);
    }

    @Override
    public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(final String domain, final String code, final boolean withSubCodes) {
        verifyReferenceDomain(domain);
        verifyReferenceCode(domain, code);

        return referenceCodeRepository.getReferenceCodeByDomainAndCode(domain, code, withSubCodes);
    }

    private void verifyReferenceDomain(final String domain) {
        referenceCodeRepository.getReferenceDomain(domain)
                .orElseThrow(EntityNotFoundException.withMessage("Reference domain [%s] not found.", domain));
    }

    private void verifyReferenceCode(final String domain, final String code) {
        referenceCodeRepository.getReferenceCodeByDomainAndCode(domain, code, false)
                .orElseThrow(EntityNotFoundException.withMessage("Reference code for domain [%s] and code [%s] not found.", domain, code));
    }

    @Override
    public List<ReferenceCode> getScheduleReasons(final String eventType) {
        verifyReferenceCode(ReferenceDomain.INTERNAL_SCHEDULE_TYPE.getDomain(), eventType);

        final var scheduleReasons = referenceCodeRepository.getScheduleReasons(eventType);
        return tidyDescriptionAndSort(scheduleReasons);
    }

    @Override
    public boolean isReferenceCodeActive(final String domain, final String code) {
        // Call the advised version of the repository so that cacheing is applied.
        return referenceCodeRepository
                .getReferenceCodeByDomainAndCode(domain, code, false)
                .map(rc -> "Y".equals(rc.getActiveFlag()))
                .orElse(false);
    }

    private List<ReferenceCode> tidyDescriptionAndSort(final List<ReferenceCode> refCodes) {
        return refCodes.stream()
                .map(p -> ReferenceCode.builder()
                        .code(p.getCode())
                        .description(WordUtils.capitalizeFully(p.getDescription()))
                        .build())
                .sorted(Comparator.comparing(ReferenceCode::getDescription))
                .collect(Collectors.toList());
    }
}
