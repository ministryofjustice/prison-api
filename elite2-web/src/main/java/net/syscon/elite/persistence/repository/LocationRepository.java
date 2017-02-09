package net.syscon.elite.persistence.repository;


import net.syscon.elite.web.api.model.Location;

import java.util.List;

public interface LocationRepository {

	List<Location> findLocationsByAgencyId(String agencyId, final int offset, final int limit);


}
