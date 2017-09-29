package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class ReferenceDomainServiceImpl implements ReferenceDomainService {

	@Autowired
	private ReferenceCodeRepository referenceCodeRepository;

	@Autowired
	private CaseLoadRepository caseLoadRepository;

	private String getDefaultOrderBy(String orderBy) {
		return StringUtils.isEmpty(orderBy)? "code": orderBy;
	}

	@Override
	public List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		return referenceCodeRepository.getReferenceCodesByDomain("ALERT", query, getDefaultOrderBy(orderBy), order, offset, limit, includeSubTypes);
	}

	@Override
	public ReferenceCode getAlertTypeByCode(String alertType) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("ALERT", alertType).orElseThrow(new EntityNotFoundException(alertType));
	}

	@Override
	public List<ReferenceCode> getAlertTypesByParent(String alertType, String query, String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("ALERT_CODE", alertType, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public ReferenceCode getAlertTypeByParentAndCode(String alertType, String alertCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndParentAndCode("ALERT_CODE", alertType, alertCode).orElseThrow(new EntityNotFoundException(alertType+"/"+alertCode));
	}

	@Override
	public ReferenceCode getCaseNoteType(String typeCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("TASK_TYPE", typeCode).orElseThrow(new EntityNotFoundException(typeCode));
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		return referenceCodeRepository.getReferenceCodesByDomain("TASK_TYPE", query,  getDefaultOrderBy(orderBy), order, offset, limit, includeSubTypes);
	}

	@Override
	public ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndParentAndCode("TASK_SUBTYPE", typeCode, subTypeCode).orElseThrow(new EntityNotFoundException(typeCode+"/"+subTypeCode));
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("TASK_SUBTYPE", typeCode, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSources(String query, String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("NOTE_SOURCE", query, getDefaultOrderBy(orderBy), order, offset, limit, false);
	}

	@Override
	public ReferenceCode getCaseNoteSource(String sourceCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("NOTE_SOURCE", sourceCode).orElseThrow(new EntityNotFoundException(sourceCode));
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypesByParent(final String caseNoteType, final long offset, final long limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("TASK_SUBTYPE", caseNoteType, "", "code", Order.ASC, offset, limit);
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		final Optional<CaseLoad> caseLoad = caseLoadRepository.getCurrentCaseLoadDetail(UserSecurityUtils.getCurrentUsername());
		final String caseLoadType = caseLoad.isPresent() ? caseLoad.get().getType() : "BOTH";

		return referenceCodeRepository.getCaseNoteTypeByCurrentCaseLoad(caseLoadType, includeSubTypes, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubType(String typeCode, String query, String orderBy, Order order, long offset, long limit) {
		return referenceCodeRepository.getCaseNoteSubType(typeCode, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}
}
