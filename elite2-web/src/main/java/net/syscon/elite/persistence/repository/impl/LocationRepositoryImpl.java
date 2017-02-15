package net.syscon.elite.persistence.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.exception.RowMappingException;
import net.syscon.elite.persistence.repository.LocationRepository;
import net.syscon.elite.web.api.model.Location;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class LocationRepositoryImpl extends RepositoryBase implements LocationRepository {

	private final Map<String, String> locationMapping = new ImmutableMap.Builder<String, String>()
		.put("INTERNAL_LOCATION_ID", "locationId")
		.put("AGENCY_ID", "agencyId")
		.put("INTERNAL_LOCATION_TYPE", "locationType")
		.put("DESCRIPTION", "description")
		.put("AGENCY_LOCATION_TYPE", "agencyType")
		.put("PARENT_INTERNAL_LOCATION_ID", "parentLocationId")
		.put("NO_OF_OCCUPANT", "currentOccupancy").build();

	@Override
	public Location findLocation(final Long locationId) {
		final String sql = getQuery("FIND_LOCATION");
		final RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
		return jdbcTemplate.queryForObject(sql, createParams("locationId", locationId), locationRowMapper);
	}

	@Override
	public List<Location> findLocations(final int offset, final int limit) {
		final String sql = getPagedQuery("FIND_ALL_LOCATIONS");
		final RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
		return jdbcTemplate.query(sql, createParams("offset", offset, "limit", limit), locationRowMapper);
	}

	@Override
	public List<Location> findLocationsByAgencyId(final String agencyId, final int offset, final int limit) {
		final String sql = getPagedQuery("FIND_LOCATIONS_BY_AGENCY_ID");
		final RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
		return jdbcTemplate.query(sql, createParams("agencyId", agencyId, "offset", offset, "limit", limit), locationRowMapper);
	}
}


