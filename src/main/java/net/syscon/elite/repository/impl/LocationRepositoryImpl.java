package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.support.LocationProcessor;
import net.syscon.util.IQueryBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LocationRepositoryImpl extends RepositoryBase implements LocationRepository {
	private static final StandardBeanPropertyRowMapper<Location> LOCATION_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(Location.class);

	@Override
	public Optional<Location> getLocation(long locationId) {
		String sql = getQuery("GET_LOCATION");

		try {
			Location rawLocation = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId),
					LOCATION_ROW_MAPPER);

			return Optional.of( LocationProcessor.processLocation(rawLocation, true));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Location> findLocation(long locationId, String username) {
		String sql = getQuery("FIND_LOCATION");

		try {
			Location rawLocation = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId, "username", username),
                    LOCATION_ROW_MAPPER);

			return Optional.of(LocationProcessor.processLocation(rawLocation, true));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	@Cacheable("findLocationsByAgencyAndType")
	public List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, boolean noParentLocation) {
		String initialSql = getQuery("FIND_LOCATIONS_BY_AGENCY_AND_TYPE");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);

		if (noParentLocation) {
			builder = builder.addQuery("parentLocationId:is:null");
		}
		String sql = builder
				.addOrderBy(Order.ASC, "description")
				.build();

		List<Location> rawLocations = jdbcTemplate.query(
				sql,
				createParams(
						"agencyId", agencyId,
						"locationType", locationType),
				LOCATION_ROW_MAPPER);

		return LocationProcessor.processLocations(rawLocations, true);
	}
}
