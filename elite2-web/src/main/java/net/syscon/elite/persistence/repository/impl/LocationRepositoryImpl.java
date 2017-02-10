package net.syscon.elite.persistence.repository.impl;

import net.syscon.elite.persistence.mapping.LocationMapping;
import net.syscon.elite.persistence.repository.LocationRepository;
import net.syscon.elite.web.api.model.Location;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LocationRepositoryImpl extends RepositoryBase implements LocationRepository {

	private final LocationMapping locationMapping = new LocationMapping();

	@Override
	public List<Location> findLocationsByAgencyId(final String agencyId, final int offset, final int limit) {
		final String sql = getPagedQuery("FIND_LOCATIONS_BY_AGENCY_ID");
		return jdbcTemplate.query(sql, createParams("agencyId", agencyId, "offset", offset, "limit", limit), locationMapping);
	}
}


