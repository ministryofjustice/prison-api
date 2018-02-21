package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
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

		Location location;

		try {
			Location rawLocation = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId),
					LOCATION_ROW_MAPPER);

			location = LocationProcessor.processLocation(rawLocation, true);
		} catch (EmptyResultDataAccessException e) {
			location = null;
		}

		return Optional.ofNullable(location);
	}

	@Override
	public Optional<Location> findLocation(long locationId, String username) {
		String sql = getQuery("FIND_LOCATION");

		Location location;

		try {
			Location rawLocation = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId, "username", username),
                    LOCATION_ROW_MAPPER);

			location = LocationProcessor.processLocation(rawLocation, true);
		} catch (EmptyResultDataAccessException e) {
			location = null;
		}

		return Optional.ofNullable(location);
	}

	@Override
	public Page<Location> findLocations(String username, String query, String orderByField, Order order, long offset, long limit) {
		String initialSql = getQuery("FIND_ALL_LOCATIONS");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order, orderByField)
				.addPagination()
				.build();

		PageAwareRowMapper<Location> paRowMapper = new PageAwareRowMapper<>(LOCATION_ROW_MAPPER);

		List<Location> rawLocations = jdbcTemplate.query(
				sql,
				createParams("username", username, "offset", offset, "limit", limit),
				paRowMapper);

		List<Location> locations = LocationProcessor.processLocations(rawLocations, true);

		return new Page<>(locations, paRowMapper.getTotalRecords(), offset, limit);
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
