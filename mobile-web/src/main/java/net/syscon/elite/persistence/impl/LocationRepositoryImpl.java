package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.LocationsResource.Order;
import net.syscon.util.QueryBuilder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class LocationRepositoryImpl extends RepositoryBase implements LocationRepository {

	private final Map<String, FieldMapper> locationMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("INTERNAL_LOCATION_ID", 		new FieldMapper("locationId"))
		.put("AGENCY_ID", 					new FieldMapper("agencyId"))
		.put("INTERNAL_LOCATION_TYPE", 		new FieldMapper("locationType"))
		.put("DESCRIPTION", 				new FieldMapper("description"))
		.put("AGENCY_LOCATION_TYPE", 		new FieldMapper("agencyType"))
		.put("PARENT_INTERNAL_LOCATION_ID", new FieldMapper("parentLocationId"))
		.put("NO_OF_OCCUPANT", 				new FieldMapper("currentOccupancy")).build();

	@Override
	public Location findLocation(final String caseLoadId, final Long locationId) {
		final String sql = getQuery("FIND_LOCATION");
		final RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
		return jdbcTemplate.queryForObject(sql, createParams("caseLoadId", caseLoadId, "locationId", locationId), locationRowMapper);
	}

	@Override
	public List<Location> findLocations(final String caseLoadId, String query, String orderByField, Order order, final int offset, final int limit) {
		//final String sql = getPagedQuery("FIND_ALL_LOCATIONS");
		final String sql = new QueryBuilder.Builder(getQuery("FIND_ALL_LOCATIONS"), locationMapping).
				addQuery(query).
				addOrderBy(order == Order.asc, orderByField).
				addPagedQuery()
				.build();
		final RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
		return jdbcTemplate.query(sql, createParams("caseLoadId", caseLoadId, "offset", offset, "limit", limit), locationRowMapper);
	}

	@Override
	public List<Location> findLocationsByAgencyId(final String caseLoadId, final String agencyId, final String query, final int offset, final int limit, final String orderByField, final String order) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_LOCATIONS_BY_AGENCY_ID"), locationMapping).
						addQuery(query).
						addOrderBy("asc".equalsIgnoreCase(order)?true:false, orderByField).
						addPagedQuery()
						.build();
		final RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
		return jdbcTemplate.query(sql, createParams("caseLoadId", caseLoadId, "agencyId", agencyId, "offset", offset, "limit", limit), locationRowMapper);
	}
}


