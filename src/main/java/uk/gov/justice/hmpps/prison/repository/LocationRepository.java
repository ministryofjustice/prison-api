package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.LocationRepositorySql;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class LocationRepository extends RepositoryBase {
    private static final StandardBeanPropertyRowMapper<Location> LOCATION_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Location.class);

    public Optional<Location> findLocation(final long locationId, final StatusFilter filter) {
        final var sql = LocationRepositorySql.GET_LOCATION.getSql();

        try {
            final var rawLocation = jdbcTemplate.queryForObject(
                    sql,
                    createParams("locationId", locationId, "activeFlag", filter.getActiveFlag()),
                    LOCATION_ROW_MAPPER);

            return Optional.of(LocationProcessor.processLocation(rawLocation, true));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<Location> findLocation(final long locationId, final String username) {
        final var sql = LocationRepositorySql.FIND_LOCATION.getSql();

        try {
            final var rawLocation = jdbcTemplate.queryForObject(
                    sql,
                    createParams("locationId", locationId, "username", username),
                    LOCATION_ROW_MAPPER);

            return Optional.of(LocationProcessor.processLocation(rawLocation, true));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Location> getLocationGroupData(final String agencyId) {
        return jdbcTemplate.query(
                LocationRepositorySql.GET_LOCATION_GROUP_DATA.getSql(),
                Map.of("agencyId", agencyId),
                LOCATION_ROW_MAPPER);
    }

    public List<Location> getSubLocationGroupData(Set<Long> parentLocationIds) {
        if (CollectionUtils.isEmpty(parentLocationIds)) {
            return List.of();
        }
        return jdbcTemplate.query(
                LocationRepositorySql.GET_SUB_LOCATION_GROUP_DATA.getSql(),
                Map.of("parentLocationIds", parentLocationIds),
                LOCATION_ROW_MAPPER);
    }
}
