package net.syscon.elite.service;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.Optional;

public interface ReferenceDomainService {
	Page<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);

	ReferenceCode getAlertTypeByCode(String alertType);

	Page<ReferenceCode> getAlertTypesByParent(String alertType, String query, String orderBy, Order order, long offset, long limit);

	ReferenceCode getAlertTypeByParentAndCode(String alertType, String alertCode);

	ReferenceCode getCaseNoteType(String typeCode);

	Page<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);

	ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode);

//	Page<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, long offset, long limit);

	Page<ReferenceCode> getCaseNoteSources(String query, String orderBy, Order order, long offset, long limit);

	ReferenceCode getCaseNoteSource(String sourceCode);

	Page<ReferenceCode> getCaseNoteSubTypesByParent(String caseNoteType, long offset, long limit);

	Page<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);

//	Page<ReferenceCode> getCaseNoteSubType(String typeCode, String query, String orderBy, Order order, long offset, long limit);
}
