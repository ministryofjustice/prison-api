package uk.gov.justice.hmpps.prison.repository;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.ScheduleRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class ScheduleRepository extends RepositoryBase {

    private static final StandardBeanPropertyRowMapper<PrisonerSchedule> EVENT_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(PrisonerSchedule.class);


    public List<PrisonerSchedule> getAllActivitiesAtAgency(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order, boolean includeSuspended) {
        final var initialSql = ScheduleRepositorySql.GET_ALL_ACTIVITIES_AT_AGENCY.getSql();

        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap())
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                        "includeSuspended", includeSuspended ? Set.of("Y", "N") : Set.of("N")),
                EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getActivitiesAtLocation(final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order, boolean includeSuspended) {
        final var initialSql = ScheduleRepositorySql.GET_ACTIVITIES_AT_ONE_LOCATION.getSql();

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("locationId", locationId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                        "includeSuspended", includeSuspended ? Set.of("Y", "N") : Set.of("N")),
                EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getLocationAppointments(final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(locationId, "locationId is a required parameter");

        final var initialSql = ScheduleRepositorySql.GET_APPOINTMENTS_AT_LOCATION.getSql();

        return getScheduledEvents(initialSql, locationId, fromDate, toDate, orderByFields, order);
    }


    public List<PrisonerSchedule> getLocationVisits(final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(locationId, "locationId is a required parameter");

        final var initialSql = ScheduleRepositorySql.GET_VISITS_AT_LOCATION.getSql();

        return getScheduledEvents(initialSql, locationId, fromDate, toDate, orderByFields, order);
    }

    private List<PrisonerSchedule> getScheduledEvents(final String initialSql, final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("locationId", locationId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))), EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getVisits(final String agencyId, final List<String> offenderNo, final LocalDate date) {
        return jdbcTemplate.query(
                ScheduleRepositorySql.GET_VISITS.getSql() + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.getSql(),
                createParams(
                        "offenderNos", offenderNo,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getAppointments(final String agencyId, final List<String> offenderNo, final LocalDate date) {
        return jdbcTemplate.query(
                ScheduleRepositorySql.GET_APPOINTMENTS.getSql() + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.getSql(),
                createParams(
                        "offenderNos", offenderNo,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getActivities(final String agencyId, final List<String> offenderNumbers, final LocalDate date) {
        return jdbcTemplate.query(
                ScheduleRepositorySql.GET_ACTIVITIES.getSql() + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.getSql(),
                createParams(
                        "offenderNos", offenderNumbers,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getCourtEvents(final List<String> offenderNumbers, final LocalDate date) {
        return jdbcTemplate.query(
                ScheduleRepositorySql.GET_COURT_EVENTS.getSql(),
                createParams(
                        "offenderNos", offenderNumbers,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }


    public List<PrisonerSchedule> getExternalTransfers(final String agencyId, final List<String> offenderNumbers, final LocalDate date) {
        return jdbcTemplate.query(
                ScheduleRepositorySql.GET_EXTERNAL_TRANSFERS.getSql() + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.getSql(),
                createParams(
                        "offenderNos", offenderNumbers,
                        "agencyId", agencyId,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }
}
