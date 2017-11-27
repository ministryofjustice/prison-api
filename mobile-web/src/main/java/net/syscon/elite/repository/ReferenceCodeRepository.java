package net.syscon.elite.repository;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ReferenceDomain;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.Optional;

public interface ReferenceCodeRepository {

	Optional<ReferenceDomain> getReferenceDomain(String domain);

	Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes);

	Page<ReferenceCode> getReferenceCodesByDomain(String domain, boolean withSubCodes, String orderBy, Order order, long offset, long limit);
}
