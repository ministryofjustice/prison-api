package net.syscon.elite.repository;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;

import java.util.List;

/**
 * Location API repository interface.
 */
public interface LocationRepository {
    @Deprecated
    List<Location> findLocations(String query, String orderByField, Order order, long offset, long limit);
    List<Location> findLocationsByAgency(String agencyId, String orderByField, Order order, long offset, long limit);
    List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, int depthAllowed);
}
