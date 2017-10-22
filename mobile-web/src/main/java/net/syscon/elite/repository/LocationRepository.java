package net.syscon.elite.repository;


import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {

	Optional<Location> findLocation(long locationId);

	List<Location> findLocations(String query, String orderByField, Order order, long offset, long limit);

	List<Location> findLocationsByAgencyId(final String caseLoadId, final String agencyId, final String query, final long offset, final long limit, final String orderByField, final Order order);

	List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, int depthAllowed);
}
