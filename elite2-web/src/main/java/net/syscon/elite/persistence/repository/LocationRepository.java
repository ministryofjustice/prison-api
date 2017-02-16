package net.syscon.elite.persistence.repository;


import net.syscon.elite.web.api.model.Location;

import java.util.List;

public interface LocationRepository {

	Location findLocation(Long locationId);
	List<Location> findLocations(int offset, int limit);
	List<Location> findLocationsByAgencyId(String agencyId, final int offset, final int limit);


}
