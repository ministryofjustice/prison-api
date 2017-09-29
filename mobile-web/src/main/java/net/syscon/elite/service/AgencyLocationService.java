package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Order;

import java.util.List;

public interface AgencyLocationService {

	Agency getAgency(String agencyId);
	List<Agency> getAgencies(final int offset, final int limit);

	Location getLocation(long locationId, boolean withInmates);
	List<Location> getLocations(final String query, final String orderBy, final Order order, final long offset, final long limit);
	List<Location> getLocationsFromAgency(String agencyId, final String query, final long offset, final long limit,final String orderByField, final Order order);
	List<OffenderBooking> getInmatesFromLocation(long locationId, String query, String orderByField, Order order, long offset, long limit);
}
