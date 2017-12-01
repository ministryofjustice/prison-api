package net.syscon.elite.repository;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
	Optional<Location> findLocation(long locationId);
	Page<Location> findLocations(String query, String orderByField, Order order, long offset, long limit);
	Page<Location> findLocationsByAgencyId(String caseLoadId, String agencyId, String query, long offset, long limit, String orderByField, Order order);
	List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, int depthAllowed);
    List<String> getCells(String agencyId);
}
