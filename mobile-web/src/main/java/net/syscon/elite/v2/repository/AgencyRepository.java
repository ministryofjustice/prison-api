package net.syscon.elite.v2.repository;

import net.syscon.elite.v2.api.model.Agency;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;
import java.util.Optional;

/**
 * Agency API (v2) repository interface.
 */
public interface AgencyRepository {
    List<Agency> findAgenciesByCaseLoad(String caseLoadId, String orderByField, Order order);
    List<Agency> findAgenciesByUsername(String username, String orderByField, Order order);
    Optional<Agency> getAgency(String agencyId);
}
