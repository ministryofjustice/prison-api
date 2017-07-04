package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.CaseNoteType;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;

import java.util.List;

public interface ReferenceCodeRepository {

	List<ReferenceCode> getCaseNoteTypesByCaseLoad(String caseLoad, int offset, int limit);

	ReferenceCode getReferenceCodeByDomainAndCode(String domain, String code);
	ReferenceCode getReferenceCodeByDomainAndParentAndCode(String domain, String code, String parentCode);
	List<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, int offset, int limit);
	List<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, int offset, int limit);
	List<CaseNoteType> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, String order, int offset, int limit);
	List<CaseNoteType> getCaseNoteSubType(String typeCode, String query, String orderBy, String order, int offset, int limit);
}

