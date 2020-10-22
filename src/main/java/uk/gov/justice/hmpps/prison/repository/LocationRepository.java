package uk.gov.justice.hmpps.prison.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;

@Repository
public class LocationRepository extends RepositoryBase {
    private static final StandardBeanPropertyRowMapper<Location> LOCATION_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Location.class);


    public Optional<Location> findLocation(final long locationId) {
        return findLocation(locationId, ACTIVE_ONLY);
    }


    public Optional<Location> findLocation(final long locationId, final StatusFilter filter) {
        final var sql = getQuery("GET_LOCATION");

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
        final var sql = getQuery("FIND_LOCATION");

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

    /**
     * Return a List (a set represented by a list) of Location which match the search criteria
     *
     * @param agencyId         Location must be within the specified agency
     * @param locationType     'WING', 'CELL' etc.
     * @param noParentLocation if true exlude any Location that has a parent.
     * @return The matching set of Location.  Note that:
     * The locationPrefix is replaced by description if present and
     * The description is replaced by userDescription if it exists otherwise the description has its agency prefix removed.
     */

    @Cacheable("findLocationsByAgencyAndType")
    public List<Location> findLocationsByAgencyAndType(final String agencyId, final String locationType, final boolean noParentLocation) {
        final var initialSql = getQuery("FIND_LOCATIONS_BY_AGENCY_AND_TYPE");
        var builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);

        if (noParentLocation) {
            builder = builder.addQuery("parentLocationId:is:null");
        }
        final var sql = builder
                .addOrderBy(Order.ASC, "description")
                .build();

        final var rawLocations = jdbcTemplate.query(
                sql,
                createParams(
                        "agencyId", agencyId,
                        "locationType", locationType),
                LOCATION_ROW_MAPPER);

        return LocationProcessor.processLocations(rawLocations, true);
    }


    public List<Location> getLocationGroupData(final String agencyId) {
        return jdbcTemplate.query(
                getQuery("GET_LOCATION_GROUP_DATA"),
                Map.of("agencyId", agencyId),
                LOCATION_ROW_MAPPER);
    }


    public List<Location> getSubLocationGroupData(Set<Long> parentLocationIds) {

        return jdbcTemplate.query(
                getQuery("GET_SUB_LOCATION_GROUP_DATA"),
                Map.of("parentLocationIds", parentLocationIds),
                LOCATION_ROW_MAPPER);
    }
}
