package uk.gov.justice.hmpps.prison.repository;

import lombok.val;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.AgencyRepositorySql;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.OffenderIepReview;
import uk.gov.justice.hmpps.prison.service.OffenderIepReviewSearchCriteria;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.sql.Types;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Agency API repository implementation.
 */
@Repository
public class AgencyRepository extends RepositoryBase {

    private static final StandardBeanPropertyRowMapper<Agency> AGENCY_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Agency.class);

    private static final StandardBeanPropertyRowMapper<Location> LOCATION_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Location.class);

    private static final StandardBeanPropertyRowMapper<OffenderIepReview> OFFENDER_IEP_REVIEW_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderIepReview.class);


    public Page<Agency> getAgencies(final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = AgencyRepositorySql.GET_AGENCIES.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder
                .addRowCount()
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(AGENCY_ROW_MAPPER);

        final var agencies = jdbcTemplate.query(
                sql,
                createParams("offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(agencies, paRowMapper.getTotalRecords(), offset, limit);
    }


    public List<Agency> getAgenciesByType(final String agencyType) {
        return jdbcTemplate.query(
                AgencyRepositorySql.GET_AGENCIES_BY_TYPE.getSql(),
                createParams("agencyType", agencyType, "activeFlag", "Y", "excludeIds", List.of("OUT", "TRN")),
                AGENCY_ROW_MAPPER);
    }


    @Cacheable("findAgenciesByUsername")
    public List<Agency> findAgenciesByUsername(final String username) {
        final var initialSql = AgencyRepositorySql.FIND_AGENCIES_BY_USERNAME.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.addOrderBy(true, "agencyId").build();

        return jdbcTemplate.query(
                sql,
                createParams("username", username, "caseloadType", "INST", "caseloadFunction", "GENERAL"),
                AGENCY_ROW_MAPPER);
    }


    public List<Agency> findAgenciesForCurrentCaseloadByUsername(final String username) {
        final var initialSql = AgencyRepositorySql.FIND_AGENCIES_BY_CURRENT_CASELOAD.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.build();
        return jdbcTemplate.query(
                sql,
                createParams("username", username),
                AGENCY_ROW_MAPPER);
    }


    public List<Agency> findAgenciesByCaseload(final String caseload) {
        final var initialSql = AgencyRepositorySql.FIND_AGENCIES_BY_CASELOAD.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.build();
        return jdbcTemplate.query(
                sql,
                createParams("caseloadId", caseload),
                AGENCY_ROW_MAPPER);
    }



    public Optional<Agency> findAgency(final String agencyId, final StatusFilter filter, final String agencyType) {
        final var initialSql = AgencyRepositorySql.GET_AGENCY.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.build();

        Agency agency;

        try {
            agency = jdbcTemplate.queryForObject(sql,
                    createParams("agencyId", agencyId,
                            "activeFlag", filter.getActiveFlag(),
                            "agencyType", agencyType),
                    AGENCY_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            agency = null;
        }

        return Optional.ofNullable(agency);
    }


    public List<Location> getAgencyLocations(final String agencyId, final List<String> eventTypes, final String sortFields, final Order sortOrder) {
        final String initialSql;

        if (eventTypes.isEmpty()) {
            initialSql = AgencyRepositorySql.GET_AGENCY_LOCATIONS.getSql();
        } else {
            initialSql = AgencyRepositorySql.GET_AGENCY_LOCATIONS_FOR_EVENT_TYPE.getSql();
        }

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);

        final var sql = builder.addOrderBy(sortOrder, sortFields).build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId, "eventTypes", eventTypes),
                LOCATION_ROW_MAPPER);
    }

    public List<Location> getAgencyLocationsBooked(final String agencyId, final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        final var params = createParams("agencyId", agencyId);

        final var initialSql = AgencyRepositorySql.GET_AGENCY_LOCATIONS_FOR_EVENTS_BOOKED.getSql();
        setupDates(params, bookedOnDay, bookedOnPeriod);

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);
        final var sql = builder.build();

        return jdbcTemplate.query(sql, params, LOCATION_ROW_MAPPER);
    }

    private void setupDates(final MapSqlParameterSource params, final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        var start = bookedOnDay.atStartOfDay();
        var end = bookedOnDay.plusDays(1).atStartOfDay();
        if (bookedOnPeriod != null) {
            switch (bookedOnPeriod) {
                case AM -> end = bookedOnDay.atTime(12, 0);
                case PM -> {
                    start = bookedOnDay.atTime(12, 0);
                    end = bookedOnDay.atTime(17, 0);
                }
                case ED -> start = bookedOnDay.atTime(17, 0);
                default -> throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Unrecognised timeslot: " + bookedOnPeriod);
            }
        }
        end = end.minus(1, ChronoUnit.SECONDS);
        final var periodStart = DateTimeConverter.fromLocalDateTime(start);
        final var periodEnd = DateTimeConverter.fromLocalDateTime(end);
        params.addValue("periodStart", periodStart);
        params.addValue("periodEnd", periodEnd);
    }

    public Page<OffenderIepReview> getPrisonIepReview(final OffenderIepReviewSearchCriteria criteria) {
        val pageRequest = criteria.getPageRequest();

        val params = createParamSource(pageRequest,
                "agencyId", new SqlParameterValue(Types.VARCHAR, criteria.getAgencyId()),
                "bookingSeq", new SqlParameterValue(Types.INTEGER, 1),
                "hearingFinding", new SqlParameterValue(Types.VARCHAR, "PROVED"),
                "threeMonthsAgo", new SqlParameterValue(Types.DATE, LocalDate.now().minus(Period.ofMonths(3))),
                "iepLevel", new SqlParameterValue(Types.VARCHAR, criteria.getIepLevel()),
                "location", new SqlParameterValue(Types.VARCHAR, criteria.getLocation()));

        val results = jdbcTemplate.query(AgencyRepositorySql.GET_AGENCY_IEP_REVIEW_INFORMATION.getSql(), params, OFFENDER_IEP_REVIEW_ROW_MAPPER);

        val page = results.stream()
                .sorted(comparing(OffenderIepReview::getNegativeIeps).reversed())
                .skip(criteria.getPageRequest().getOffset())
                .limit(criteria.getPageRequest().getLimit())
                .collect(toList());

        return new Page<>(page, results.size(), pageRequest);
    }
}
