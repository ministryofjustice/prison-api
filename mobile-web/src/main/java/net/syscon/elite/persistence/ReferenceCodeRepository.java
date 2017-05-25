package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;

import java.util.List;

public interface ReferenceCodeRepository {
	List<ReferenceCode> getCnotetypesByCaseLoad(String caseLoad);
	List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNotetype);
	List<ReferenceCode> getReferenceCodesForDomain(String domain);
	ReferenceCode getReferenceCodeDescriptionForCode(String domain, String code);
	
	List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypesByAlertType(String alertType);
	List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode);
}
