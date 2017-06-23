package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.CaseNoteType;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class ReferenceDomainServiceImpl implements ReferenceDomainService {

	private final static String DEFAULT_ACTIVE_FLAG_QUERY = "activeFlag:eq:'Y'";

	private final ReferenceCodeRepository referenceCodeRepository;

	@Inject
	public ReferenceDomainServiceImpl(final ReferenceCodeRepository referenceCodeRepository) {
		this.referenceCodeRepository = referenceCodeRepository;
	}

	private String getDefaultOrderBy(String orderBy) {
		return StringUtils.isEmpty(orderBy)? "code": orderBy;
	}

	private String addDefaultQuery(String query) {
		if(query==null || "".equals(query) ) {
			query = DEFAULT_ACTIVE_FLAG_QUERY;
		} else if(!query.contains("activeFlag")) {
			query = query + ",and:"+ DEFAULT_ACTIVE_FLAG_QUERY;
		}
		return query;
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypesByCaseLoad(final String caseLoad, final int offset, final int limit) {
		return referenceCodeRepository.getCaseNoteTypesByCaseLoad(caseLoad, offset, limit);
	}

	@Override
	public List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("ALERT", query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public ReferenceCode getAlertTypeByCode(String alertType) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("ALERT", alertType);
	}

	@Override
	public List<ReferenceCode> getAlertTypesByParent(String alertType, String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("ALERT_CODE", alertType, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public ReferenceCode getAlertTypeByParentAndCode(String alertType, String alertCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndParentAndCode("ALERT_CODE", alertType, alertCode);
	}

	@Override
	public ReferenceCode getCaseNoteType(String typeCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("TASK_TYPE", typeCode);
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("TASK_TYPE", query,  getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndParentAndCode("TASK_SUBTYPE", typeCode, subTypeCode);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("TASK_SUBTYPE", typeCode, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSources(String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getReferenceCodesByDomain("NOTE_SOURCE", query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public ReferenceCode getCaseNoteSource(String sourceCode) {
		return referenceCodeRepository.getReferenceCodeByDomainAndCode("NOTE_SOURCE", sourceCode);
	}




	// TODO: Remove this method after IG change to the new the endpoint (getCnoteSubtypesByCaseNoteType => getCaseNoteSubTypesByParent)
	@Override
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNoteType, int offset, int limit) {
		return referenceCodeRepository.getCnoteSubtypesByCaseNoteType(caseNoteType, offset, limit);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypesByParent(final String caseNoteType, final int offset, final int limit) {
		return referenceCodeRepository.getReferenceCodesByDomainAndParent("TASK_SUBTYPE", caseNoteType, "", "code", Order.asc, offset, limit);
	}

	@Override
	public List<CaseNoteType> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, String order, int offset, int limit) {
		return referenceCodeRepository.getCaseNoteTypeByCurrentCaseLoad(query, getDefaultOrderBy(orderBy), order, offset, limit);
	}
	
	@Override
	public List<CaseNoteType> getCaseNoteSubType(String typeCode, String query, String orderBy, String order, int offset, int limit) {
		return referenceCodeRepository.getCaseNoteSubType(typeCode, query, getDefaultOrderBy(orderBy), order, offset, limit);
	}

}
