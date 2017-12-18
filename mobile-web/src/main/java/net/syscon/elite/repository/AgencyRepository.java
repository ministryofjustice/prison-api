package net.syscon.elite.repository;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetails;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

/**
 * Agency API repository interface.
 */
public interface AgencyRepository {
    Page<Agency> getAgencies(String orderByField, Order order, long offset, long limit);
    List<Agency> findAgenciesByUsername(String username);
    Optional<Agency> getAgency(String agencyId);
    List<Location> getAvailableLocations(String agencyId, String eventType);
    List<PrisonContactDetails> getPrisonContactDetails(String agencyId);
}
