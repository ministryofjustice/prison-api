package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.LocationsResource.Order;

import java.util.List;

public interface LocationRepository {
	Location findLocation(String caseLoadId, Long locationId);
	List<Location> findLocations(String caseLoadId, String query, String orderByField, Order order, int offset, int limit);
	List<Location> findLocationsByAgencyId(String caseLoadId, final String agencyId, final String query, final int offset, final int limit, final String orderByField, final String order);
}
