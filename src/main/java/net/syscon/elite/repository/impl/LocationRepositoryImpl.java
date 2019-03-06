package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.support.LocationProcessor;
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
    public Optional<Location> getLocation(final long locationId) {
        final var sql = getQuery("GET_LOCATION");

		try {
            final var rawLocation = jdbcTemplate.queryForObject(
					sql,
					createParams("locationId", locationId),
					LOCATION_ROW_MAPPER);

			return Optional.of( LocationProcessor.processLocation(rawLocation, true));
        } catch (final EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
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

	@Override
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
}
