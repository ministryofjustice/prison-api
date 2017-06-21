package net.syscon.elite.service;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;

import java.util.List;



public interface ReferenceDomainService {

	List<ReferenceCode> getCaseNoteTypesByCaseLoad(String caseLoad, int offset, int limit);
	List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypeByCode(String alertType);
	List<ReferenceCode> getAlertTypesByParent(String alertType, String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getAlertTypeByParentAndCode(String alertType, String alertCode);
	ReferenceCode getCaseNoteType(String typeCode);
	List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode);
	List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit);
	List<ReferenceCode> getCaseNoteSources(String query, String orderBy, Order order, int offset, int limit);
	ReferenceCode getCaseNoteSource(String sourceCode);

	// TODO: Remove this method after IG change to the new the endpoint (getCnoteSubtypesByCaseNoteType => getCaseNoteSubTypesByParent)
	List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNoteType, int offset, int limit);
	List<ReferenceCode> getCaseNoteSubTypesByParent(String caseNoteType, int offset, int limit);


}
