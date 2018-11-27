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

    Page<OffenderBooking> getInmatesFromLocation(long locationId, String username, String query, String orderByField, Order order, long offset, long limit);

    List<Location> getCellLocationsForGroup(String agencyId, String name);
}