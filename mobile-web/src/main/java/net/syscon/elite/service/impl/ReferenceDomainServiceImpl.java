package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;
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

	@Override
	public List<ReferenceCode> getCaseNoteTypesByCaseLoad(final String caseLoad, final int offset, final int limit) {
		return referenceCodeRepository.getCaseNoteTypesByCaseLoad(caseLoad, offset, limit);
	}
	@Override
	public List<ReferenceCode> getCaseNoteSubTypesByCaseNoteType(final String caseNotetype, final int offset, final int limit) {
		return referenceCodeRepository.getCaseNoteSubTypesByCaseLoad(caseNotetype, offset, limit);
	}
	@Override
	public List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit) {
		orderBy = getDefaultOrderBy(orderBy);
		return referenceCodeRepository.getAlertTypes(addDefaultQuery(query), orderBy, order, offset, limit);
	}

	private String getDefaultOrderBy(String orderBy) {
		if(orderBy == null || "".equals(orderBy)) {
			orderBy = "code";
		}
		return orderBy;
	}

	@Override
	public ReferenceCode getAlertTypesByAlertType(String alertType) {
		return referenceCodeRepository.getAlertTypesByAlertType(alertType);
	}
	@Override
	public List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order,  int offset, int limit) {
		return referenceCodeRepository.getAlertTypesByAlertTypeCode(alertType, addDefaultQuery(query),  getDefaultOrderBy(orderBy), order, offset, limit);
	}
	@Override
	public ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode) {
		return referenceCodeRepository.getAlertTypeCodesByAlertCode(alertType, alertCode);
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomain(String domain, int offset, int limit) {
		return referenceCodeRepository.getReferenceCodesByDomain(domain, offset, limit);
	}

	@Override
	public ReferenceCode getReferenceCodeByDomainAndCode(String domain, String code) {
		return referenceCodeRepository.getReferenceCodesByDomainAndCode(domain, code);
	}

	@Override
	public ReferenceCode getCaseNoteType(String typeCode) {
		return referenceCodeRepository.getCaseNoteType(typeCode);
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getCaseNoteTypes(query,  getDefaultOrderBy(orderBy), order, offset, limit);
	}

	@Override
	public ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode) {
		return referenceCodeRepository.getCaseNoteSubType(typeCode, subTypeCode);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit) {
		return referenceCodeRepository.getCaseNoteSubTypes(typeCode, query,  getDefaultOrderBy(orderBy), order, offset, limit);
	}

	private String addDefaultQuery(String query) {
		if(query==null || "".equals(query) ) {
			query = DEFAULT_ACTIVE_FLAG_QUERY;
		} else if(!query.contains("activeFlag")) {
			query = query + ",and:"+ DEFAULT_ACTIVE_FLAG_QUERY;
		}
		return query;
	}

	

}
