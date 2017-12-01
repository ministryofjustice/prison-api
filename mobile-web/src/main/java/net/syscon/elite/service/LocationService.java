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
    Location getLocation(long locationId, boolean withInmates);
    Page<Location> getLocations(String query, String orderBy, Order order, long offset, long limit);
    Page<Location> getLocationsFromAgency(String agencyId, String query, long offset, long limit, String orderByField, Order order);
    Page<OffenderBooking> getInmatesFromLocation(long locationId, String query, String orderByField, Order order, long offset, long limit);
    List<String> getGroup(String agencyId, String name);
}
