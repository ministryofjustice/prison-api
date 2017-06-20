package net.syscon.elite.service;

import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.LocationsResource.Order;

import java.util.List;


public interface AgencyLocationService {

	Agency getAgency(String agencyId);
	List<Agency> getAgencies(final int offset, final int limit);

	Location getLocation(Long locationId, boolean withInmates);
	List<Location> getLocations(final String query, final String orderBy, final Order order, final int offset, final int limit);
	List<Location> getLocationsFromAgency(String agencyId, final String query, final int offset, final int limit,final String orderByField, final String order);
	List<AssignedInmate> getInmatesFromLocation(Long locationId, String query, String orderByField, Order order, int offset, int limit);

}


