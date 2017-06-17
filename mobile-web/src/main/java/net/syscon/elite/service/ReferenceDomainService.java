package net.syscon.elite.service;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;

import java.util.List;



public interface ReferenceDomainService {
	
	List<ReferenceCode> getCaseNoteTypesByCaseLoad(String caseLoad, int offset, int limit);
	List<ReferenceCode> getCaseNoteSubTypesByCaseNoteType(String caseNoteType, int offset, int limit);
	List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypesByAlertType(String alertType);
	List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode);
	List<ReferenceCode> getReferenceCodesByDomain(String domain, int offset, int limit);
	ReferenceCode getReferenceCodeByDomainAndCode(String domain, String code);
	ReferenceCode getCaseNoteType(String typeCode);
	List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode);
	List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit);
}
