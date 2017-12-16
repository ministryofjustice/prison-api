package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class LocationRepositoryImpl extends RepositoryBase implements LocationRepository {

	private final Map<String, FieldMapper> locationMapping  =
			new ImmutableMap.Builder<String, FieldMapper>()
					.put("INTERNAL_LOCATION_ID", new FieldMapper("locationId"))
					.put("AGY_LOC_ID", new FieldMapper("agencyId"))
					.put("INTERNAL_LOCATION_TYPE", new FieldMapper("locationType"))
					.put("DESCRIPTION", new FieldMapper("description"))
					.put("AGENCY_LOCATION_TYPE", new FieldMapper("agencyType"))
					.put("PARENT_INTERNAL_LOCATION_ID", new FieldMapper("parentLocationId"))
					.put("NO_OF_OCCUPANT", new FieldMapper("currentOccupancy"))
					.put("LOCATION_PREFIX", new FieldMapper("locationPrefix"))
					.put("LEVEL", new FieldMapper("level"))
					.put("LIST_SEQ", new FieldMapper("listSequence"))
					.build();

	@Override
	public Optional<Location> getLocation(long locationId) {
		String sql = getQuery("GET_LOCATION");

		RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);

		Location location;

		try {
			location = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId),
					locationRowMapper);

			location.setDescription(removeAgencyId(location.getDescription(), location.getAgencyId()));
		} catch (EmptyResultDataAccessException e) {
			location = null;
		}

		return Optional.ofNullable(location);
	}

	@Override
	public Optional<Location> findLocation(long locationId, String username) {
		String sql = getQuery("FIND_LOCATION");

		RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);

		Location location;

		try {
			location = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId, "username", username),
					locationRowMapper);

			location.setDescription(removeAgencyId(location.getDescription(), location.getAgencyId()));
		} catch (EmptyResultDataAccessException e) {
			location = null;
		}

		return Optional.ofNullable(location);
	}

	@Override
	public Page<Location> findLocations(String username, String query, String orderByField, Order order, long offset, long limit) {
		String initialSql = getQuery("FIND_ALL_LOCATIONS");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, locationMapping);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order, orderByField)
				.addPagination()
				.build();

		RowMapper<Location> locationRowMapper =
				Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);

		PageAwareRowMapper<Location> paRowMapper = new PageAwareRowMapper<>(locationRowMapper);

		List<Location> locations = removeAgencyId(jdbcTemplate.query(
				sql,
				createParams("username", username, "offset", offset, "limit", limit),
				paRowMapper));

		return new Page<>(locations, paRowMapper.getTotalRecords(), offset, limit);
	}

	@Override
	@Cacheable("findLocationsByAgencyAndType")
	public List<Location> findLocationsByAgencyAndType(String agencyId, String locationType, int depthAllowed) {
		String initialSql = getQuery("FIND_LOCATIONS_BY_AGENCY_AND_TYPE");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, locationMapping);
		String sql = builder.build();
		RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);

		return removeAgencyId(jdbcTemplate.query(
				sql,
				createParams(
						"agencyId", agencyId,
						"locationType", locationType,
						"depth", depthAllowed),
				locationRowMapper));
	}

	private static List<Location> removeAgencyId(final List<Location> locations) {
		if (locations != null) {
			locations.forEach(l -> l.setDescription(removeAgencyId(l.getDescription(), l.getAgencyId())));
		}
		return locations;
	}

	public static String removeAgencyId(final String description, final String agencyId) {
		return StringUtils.replaceFirst(description,StringUtils.trimToEmpty(agencyId)+"-", "");
	}
}
