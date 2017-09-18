package net.syscon.elite.v2.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.impl.RepositoryBase;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.v2.repository.LocationRepository;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Location API (v2) repository implementation.
 */
@Repository(value = "locationRepositoryV2")
public class LocationRepositoryImpl extends RepositoryBase implements LocationRepository {
    private final Map<String, FieldMapper> locationMapping =
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
    @Deprecated
    @Override
    public List<Location> findLocations(String query, String orderByField, Order order, long offset, long limit) {
        String initialSql = getQuery("FIND_ALL_LOCATIONS");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, locationMapping);
        boolean isAscendingOrder = (order == Order.ASC);

        String sql = builder
                .addRowCount()
                .addQuery(query)
                .addOrderBy(isAscendingOrder, orderByField)
                .addPagination()
                .build();

        RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);

        return jdbcTemplate.query(
                sql,
                createParams("username", UserSecurityUtils.getCurrentUsername(),
                        "offset", offset,
                        "limit", limit),
                locationRowMapper);
    }

    @Override
    public List<Location> findLocationsByAgency(String agencyId, String locationType, int depthAllowed) {
        String initialSql = getQuery(StringUtils.isBlank(locationType) ? "FIND_LOCATIONS_BY_AGENCY" : "FIND_LOCATIONS_BY_AGENCY_AND_TYPE");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, locationMapping);

        String sql = builder.build();

        RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);

        return jdbcTemplate.query(
                sql,
                createParams(
                        "agencyId", agencyId,
                        "locationType", locationType,
                        "depth", depthAllowed),
                locationRowMapper);
    }
}
