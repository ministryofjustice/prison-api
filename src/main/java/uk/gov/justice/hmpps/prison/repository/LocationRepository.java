package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.LocationRepositorySql;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.Optional;

@Repository
public class LocationRepository extends RepositoryBase {
    private static final RowMapper<LocationDto> LOCATION_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(LocationDto.class);

    public Optional<Location> findLocation(final long locationId, final StatusFilter filter) {
        final var sql = LocationRepositorySql.GET_LOCATION.getSql();

        try {
            final var rawLocation = jdbcTemplate.queryForObject(
                    sql,
                    createParams("locationId", locationId, "activeFlag", filter.getActiveYesNo()),
                    LOCATION_ROW_MAPPER);

            return Optional.ofNullable(rawLocation).map(l -> LocationProcessor.processLocation(l.toLocation(), true, false));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public void findLocation(final long locationId, final String username) {
        final var sql = LocationRepositorySql.FIND_LOCATION.getSql();

        try {
            jdbcTemplate.queryForObject(
                    sql,
                    createParams("locationId", locationId, "username", username),
                    LOCATION_ROW_MAPPER);

        } catch (final EmptyResultDataAccessException e) {
            // catch empty
        }
    }
}
