package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;



public interface ReferenceDomainService {
	
	public List<ReferenceCode> getCnotetypesByCaseLoad(String caseLoad, int offset, int limit);
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNoteType, int offset, int limit);
	public List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit);
	public ReferenceCode getAlertTypesByAlertType(String alertType);
	public List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order, int offset, int limit);
	public ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode);
	
	
	
	public List<ReferenceCode> getReferencecodesForDomain(String domain);
	public ReferenceCode getRefrenceCodeDescriptionForCode(String domain, String code);
	
	
	
	
}
