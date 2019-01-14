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

    public ReferenceDomainServiceImpl(ReferenceCodeRepository referenceCodeRepository) {
        this.referenceCodeRepository = referenceCodeRepository;
    }

    private static String getDefaultOrderBy(String orderBy) {
        return StringUtils.defaultIfBlank(orderBy, "code");
    }

    private static Order getDefaultOrder(Order order) {
        return Objects.isNull(order) ? Order.ASC : order;
    }

    @Override
    public Page<ReferenceCode> getAlertTypes(String orderBy, Order order, long offset, long limit) {
        return referenceCodeRepository.getReferenceCodesByDomain(
                ReferenceDomain.ALERT.getDomain(), true,
                getDefaultOrderBy(orderBy), getDefaultOrder(order),
                offset, limit);
    }

    @Override
    public Page<ReferenceCode> getCaseNoteSources(String orderBy, Order order, long offset, long limit) {
        return referenceCodeRepository.getReferenceCodesByDomain(
                ReferenceDomain.CASE_NOTE_SOURCE.getDomain(), false,
                getDefaultOrderBy(orderBy), getDefaultOrder(order),
                offset, limit);
    }

    @Override
    public Page<ReferenceCode> getReferenceCodesByDomain(String domain, boolean withSubCodes, String orderBy, Order order, long offset, long limit) {
        verifyReferenceDomain(domain);

        return referenceCodeRepository.getReferenceCodesByDomain(
                domain, withSubCodes, getDefaultOrderBy(orderBy), getDefaultOrder(order), offset, limit);
    }

    @Override
    public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes) {
        verifyReferenceDomain(domain);
        verifyReferenceCode(domain, code);

        return referenceCodeRepository.getReferenceCodeByDomainAndCode(domain, code, withSubCodes);
    }

    private void verifyReferenceDomain(String domain) {
        referenceCodeRepository.getReferenceDomain(domain)
                .orElseThrow(EntityNotFoundException.withMessage("Reference domain [%s] not found.", domain));
    }

    private void verifyReferenceCode(String domain, String code) {
        referenceCodeRepository.getReferenceCodeByDomainAndCode(domain, code, false)
                .orElseThrow(EntityNotFoundException.withMessage("Reference code for domain [%s] and code [%s] not found.", domain, code));
    }

    @Override
    public List<ReferenceCode> getScheduleReasons(String eventType) {
        verifyReferenceCode(ReferenceDomain.INTERNAL_SCHEDULE_TYPE.getDomain(), eventType);

        List<ReferenceCode> scheduleReasons = referenceCodeRepository.getScheduleReasons(eventType);
        return tidyDescriptionAndSort(scheduleReasons);
    }

    @Override
    public boolean isReferenceCodeActive(String domain, String code) {
        // Call the advised version of the repository so that cacheing is applied.
        return referenceCodeRepository
                .getReferenceCodeByDomainAndCode(domain, code, false)
                .map(rc -> "Y".equals(rc.getActiveFlag()))
                .orElse(false);
    }

    private List<ReferenceCode> tidyDescriptionAndSort(List<ReferenceCode> refCodes) {
        return refCodes.stream()
                .map(p -> ReferenceCode.builder()
                        .code(p.getCode())
                        .description(WordUtils.capitalizeFully(p.getDescription()))
                        .build())
                .sorted(Comparator.comparing(ReferenceCode::getDescription))
                .collect(Collectors.toList());
    }
}
