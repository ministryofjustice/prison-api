package net.syscon.elite.persistence;



import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
	Optional<Location> findLocation(long locationId);
	List<Location> findLocations(String query, String orderByField, Order order, long offset, long limit);
	List<Location> findLocationsByAgencyId(String caseLoadId, String agencyId, String query, long offset, long limit, String orderByField, Order order);
}
