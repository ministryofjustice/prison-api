package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;

import java.util.List;

public interface ReferenceCodeRepository {

	List<ReferenceCode> getCaseNoteTypesByCaseLoad(String caseLoad, int offset, int limit);

	ReferenceCode getReferenceCodeByDomainAndCode(String domain, String code);
	ReferenceCode getReferenceCodeByDomainAndParentAndCode(String domain, String code, String parent);
	List<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, int offset, int limit);
	List<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parent, String query, String orderBy, Order order, int offset, int limit);


	// TODO: Remove this method after IG change to the new the endpoint
	List<ReferenceCode> getCnoteSubtypesByCaseNoteType(String caseNoteType, int offset, int limit);
}

