package net.syscon.elite.service;

import net.syscon.elite.v2.api.model.ReferenceCode;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;


public interface ReferenceDomainService {

	List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);
	ReferenceCode getAlertTypeByCode(String alertType);
	List<ReferenceCode> getAlertTypesByParent(String alertType, String query, String orderBy, Order order, long offset, long limit);
	ReferenceCode getAlertTypeByParentAndCode(String alertType, String alertCode);
	ReferenceCode getCaseNoteType(String typeCode);
	List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);
	ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode);
	List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, long offset, long limit);
	List<ReferenceCode> getCaseNoteSources(String query, String orderBy, Order order, long offset, long limit);
	ReferenceCode getCaseNoteSource(String sourceCode);
	List<ReferenceCode> getCaseNoteSubTypesByParent(String caseNoteType, long offset, long limit);
	List<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);
	List<ReferenceCode> getCaseNoteSubType(String typeCode, String query, String orderBy, Order order, long offset, long limit);

}
