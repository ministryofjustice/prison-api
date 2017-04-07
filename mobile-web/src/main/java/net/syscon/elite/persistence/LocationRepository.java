package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.Location;

import java.util.List;

public interface LocationRepository {

	Location findLocation(Long locationId);
	List<Location> findLocations(int offset, int limit);
	List<Location> findLocationsByAgencyId(final String agencyId, final String query, final int offset, final int limit, final String orderByField, final String order);


}
