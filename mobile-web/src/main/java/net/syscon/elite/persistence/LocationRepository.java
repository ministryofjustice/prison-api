package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.LocationsResource.Order;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
	Optional<Location> findLocation(Long locationId);
	List<Location> findLocations(String query, String orderByField, Order order, int offset, int limit);
	List<Location> findLocationsByAgencyId(String caseLoadId, String agencyId, String query, int offset, int limit, String orderByField, Order order);
}
