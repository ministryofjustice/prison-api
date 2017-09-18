package net.syscon.elite.v2.repository;

import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;

/**
 * Location API (v2) repository interface.
 */
public interface LocationRepository {
    @Deprecated
    List<Location> findLocations(String query, String orderByField, Order order, long offset, long limit);

    List<Location> findLocationsByAgency(String agencyId, String locationType, int depthAllowed);
}
