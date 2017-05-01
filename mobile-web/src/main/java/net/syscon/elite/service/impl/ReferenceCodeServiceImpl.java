package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;


@Service
@Transactional
public class ReferenceCodeServiceImpl implements ReferenceDomainService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final String DEFAULT_ACTIVE_FLAG_QUERY = "activeFlag:eq:'Y'";
	private ReferenceCodeRepository referenceCodeRepository;
	@Inject
	public void setReferenceCodeRepository(final ReferenceCodeRepository referenceCodeRepository) {
		this.referenceCodeRepository = referenceCodeRepository;
	}
	@Override
	public List<ReferenceCode> getCnotetypesByCaseLoad(final String caseLoad) {
		return referenceCodeRepository.getCnotetypesByCaseLoad(caseLoad);
	}
	@Override
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(final String caseNotetype) {
		return referenceCodeRepository.getCnoteSubtypesByCaseNoteType(caseNotetype);
	}
	@Override
	public List<ReferenceCode> getReferencecodesForDomain(final String domain) {
		return referenceCodeRepository.getReferenceCodesForDomain(domain);
	}
	@Override
	public ReferenceCode getRefrenceCodeDescriptionForCode(final String domain, final String code) {
		return referenceCodeRepository.getReferenceCodeDescriptionForCode(domain, code);
	}
	@Override
	public List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit) {
		if(orderBy == null || "".equals(orderBy)) {
			orderBy = "code";
		}
		return referenceCodeRepository.getAlertTypes(addDefaultQuery(query), orderBy, order, offset, limit);
	}
	@Override
	public ReferenceCode getAlertTypesByAlertType(String alertType) {
		return referenceCodeRepository.getAlertTypesByAlertType(alertType);
	}
	@Override
	public List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order,  int offset, int limit) {
		if(orderBy == null || "".equals(orderBy)) {
			orderBy = "code";
		}
		return referenceCodeRepository.getAlertTypesByAlertTypeCode(alertType, addDefaultQuery(query),  orderBy, order, offset, limit);
	}
	@Override
	public ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode) {
		return referenceCodeRepository.getAlertTypeCodesByAlertCode(alertType, alertCode);
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
