package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;

import java.util.List;

public interface ReferenceCodeRepository {
	List<ReferenceCode> getCaseNoteTypesByCaseLoad(String caseLoad, int offset, int limit);
	List<ReferenceCode> getCaseNoteSubTypesByCaseLoad(String typeCode, int offset, int limit);

	List<ReferenceCode> getReferenceCodesByDomain(String domain, int offset, int limit);
	ReferenceCode getReferenceCodesByDomainAndCode(String domain, String code);
	
	List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypesByAlertType(String alertType);
	List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode);

	ReferenceCode getCaseNoteType(String typeCode);
	List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode);
	List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit);

}
