package net.syscon.elite.service;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;

/**
 * Location API service interface.
 */
public interface LocationService {
    List<Location> getUserLocations(String username);

    Location getLocation(long locationId);

    @Deprecated
    Page<Location> getLocations(String username, String query, String orderBy, Order order, long offset, long limit);

    @Deprecated
    Page<OffenderBooking> getInmatesFromLocation(long locationId, String username, String query, String orderByField, Order order, long offset, long limit);

    List<Location> getGroup(String agencyId, String name);

    List<String> getAvailableGroups(String agencyId);
}
