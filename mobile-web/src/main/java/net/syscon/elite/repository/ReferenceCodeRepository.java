package net.syscon.elite.repository;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.Optional;

public interface ReferenceCodeRepository {
	Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code);
	Optional<ReferenceCode> getReferenceCodeByDomainAndParentAndCode(String domain, String code, String parentCode);
	Page<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes);
	Page<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, long offset, long limit);
	Page<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String caseLoadType, boolean includeSubTypes, String query, String orderBy, Order order, long offset, long limit);
	Page<ReferenceCode> getCaseNoteSubType(String typeCode, String query, String orderBy, Order order, long offset, long limit);
}
