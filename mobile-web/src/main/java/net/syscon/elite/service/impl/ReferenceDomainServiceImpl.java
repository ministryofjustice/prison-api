package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.ReferenceCodeRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReferenceDomainServiceImpl implements ReferenceDomainService {
	private final ReferenceCodeRepository referenceCodeRepository;

	public ReferenceDomainServiceImpl(ReferenceCodeRepository referenceCodeRepository) {
		this.referenceCodeRepository = referenceCodeRepository;
	}

	private String getDefaultOrderBy(String orderBy) {
		return StringUtils.isEmpty(orderBy)? "code": orderBy;
	}

	@Override
    @Cacheable("alertTypes")
	public Page<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		return referenceCodeRepository.getReferenceCodesByDomain("ALERT", query, getDefaultOrderBy(orderBy), order, offset, limit, includeSubTypes);
	}

	@Override
    @Cacheable("alertTypesByType")
	public ReferenceCode getAlertTypeByCode(String alertType) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("ALERT", alertType, false).orElseThrow(EntityNotFoundException.withId(alertType));
	}

	@Override
    @Cacheable("alertTypesByTypeFiltered")
	public Page<ReferenceCode> getAlertTypesByParent(String alertType, String query, String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("ALERT_CODE", alertType, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
    @Cacheable("alertTypesByTypeAndCode")
	public ReferenceCode getAlertTypeByParentAndCode(String alertType, String alertCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndParentAndCode("ALERT_CODE", alertType, alertCode).orElseThrow(EntityNotFoundException.withId(alertType+"/"+alertCode));
	}

	@Override
    @Cacheable("caseNoteTypesByCode")
	public ReferenceCode getCaseNoteType(String typeCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("TASK_TYPE", typeCode, false).orElseThrow(EntityNotFoundException.withId(typeCode));
	}

	@Override
    @Cacheable("caseNoteTypes")
	public Page<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		return referenceCodeRepository.getReferenceCodesByDomain("TASK_TYPE", query,  getDefaultOrderBy(orderBy), order, offset, limit, includeSubTypes);
	}

	@Override
    @Cacheable("caseNoteTypesByTypeSubType")
	public ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndParentAndCode("TASK_SUBTYPE", typeCode, subTypeCode).orElseThrow(EntityNotFoundException.withId(typeCode+"/"+subTypeCode));
	}

	@Override
    @Cacheable("caseNoteSources")
	public Page<ReferenceCode> getCaseNoteSources(String query, String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("NOTE_SOURCE", query, getDefaultOrderBy(orderBy), order, offset, limit, false);
	}

	@Override
    @Cacheable("caseNoteSourcesByCode")
	public ReferenceCode getCaseNoteSource(String sourceCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("NOTE_SOURCE", sourceCode, false).orElseThrow(EntityNotFoundException.withId(sourceCode));
	}

	@Override
    @Cacheable("caseNoteTypesByType")
	public Page<ReferenceCode> getCaseNoteSubTypesByParent(final String caseNoteType, final long offset, final long limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("TASK_SUBTYPE", caseNoteType, "", "code", Order.ASC, offset, limit);
	}
}
