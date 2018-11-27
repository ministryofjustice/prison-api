package net.syscon.elite.repository;

import net.syscon.elite.api.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
	Optional<Location> getLocation(long locationId);

	@Deprecated
	Optional<Location> findLocation(long locationId, String username);

	List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, boolean noParentLocation);
}
