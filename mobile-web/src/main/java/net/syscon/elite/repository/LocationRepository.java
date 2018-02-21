package net.syscon.elite.repository;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
	Optional<Location> getLocation(long locationId);

	@Deprecated
	Optional<Location> findLocation(long locationId, String username);

	@Deprecated
	Page<Location> findLocations(String username, String query, String orderByField, Order order, long offset, long limit);

	List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, boolean noParentLocation);
}
