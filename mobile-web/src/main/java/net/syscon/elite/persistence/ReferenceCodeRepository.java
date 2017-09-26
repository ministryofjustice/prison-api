package net.syscon.elite.persistence;

import net.syscon.elite.v2.api.model.ReferenceCode;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface ReferenceCodeRepository {

	Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code);
	Optional<ReferenceCode> getReferenceCodeByDomainAndParentAndCode(String domain, String code, String parentCode);
	List<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);
	List<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, long offset, long limit);
	List<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, Order order, long offset, long limit);
	List<ReferenceCode> getCaseNoteSubType(String typeCode, String query, String orderBy, Order order, long offset, long limit);
}

