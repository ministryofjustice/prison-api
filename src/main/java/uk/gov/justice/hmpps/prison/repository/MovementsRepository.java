package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.MovementsRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

@Repository
public class MovementsRepository extends RepositoryBase {

    private static final Set<String> DEACTIVATE_REASON_CODES = Set.of("A", "C", "E", "I");
    private final StandardBeanPropertyRowMapper<Movement> MOVEMENT_MAPPER = new StandardBeanPropertyRowMapper<>(Movement.class);
    private final StandardBeanPropertyRowMapper<OffenderMovement> OFFENDER_MOVEMENT_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderMovement.class);
    private final StandardBeanPropertyRowMapper<RollCount> ROLLCOUNT_MAPPER = new StandardBeanPropertyRowMapper<>(RollCount.class);
    private final StandardBeanPropertyRowMapper<OffenderIn> OFFENDER_IN_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderIn.class);
    private final StandardBeanPropertyRowMapper<OffenderOut> OFFENDER_OUT_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderOut.class);
    private final StandardBeanPropertyRowMapper<OffenderInReception> OFFENDER_IN_RECEPTION_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderInReception.class);
    private final StandardBeanPropertyRowMapper<MovementSummary> MOVEMENT_SUMMARY_MAPPER = new StandardBeanPropertyRowMapper<>(MovementSummary.class);
    private final StandardBeanPropertyRowMapper<CourtEvent> COURT_EVENT_MAPPER = new StandardBeanPropertyRowMapper<>(CourtEvent.class);
    private final StandardBeanPropertyRowMapper<TransferEvent> OFFENDER_TRANSFER_MAPPER = new StandardBeanPropertyRowMapper<>(TransferEvent.class);
    private final StandardBeanPropertyRowMapper<ReleaseEvent> OFFENDER_RELEASE_MAPPER = new StandardBeanPropertyRowMapper<>(ReleaseEvent.class);

    private static final String MOVEMENT_DATE_CLAUSE = " AND OEM.MOVEMENT_DATE = :movementDate";


    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final List<String> movementTypes) {
        final var sql = MovementsRepositorySql.GET_RECENT_MOVEMENTS_BY_DATE_FOR_BATCH.getSql();
        final var types = (movementTypes == null || movementTypes.isEmpty()) ? Set.of("TRN", "REL", "ADM") : movementTypes;


        return jdbcTemplate.query(sql,
            createParams(
                "movementTypes", types,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(fromDateTime),
                "movementDate", DateTimeConverter.toDate(movementDate)), MOVEMENT_MAPPER);
    }


    public Optional<Movement> getMovementByBookingIdAndSequence(final long bookingId, final int sequenceNumber) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(MovementsRepositorySql.GET_MOVEMENT_BY_BOOKING_AND_SEQUENCE.getSql(),
                createParams(
                    "bookingId", bookingId,
                    "sequenceNumber", sequenceNumber),
                MOVEMENT_MAPPER));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }


    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final boolean latestOnly, final boolean allBookings) {
        final var firstSeqOnly = allBookings ? "" : "AND OB.BOOKING_SEQ = 1";
        if (movementTypes == null || movementTypes.isEmpty()) {
            return jdbcTemplate.query(format(MovementsRepositorySql.GET_MOVEMENTS_BY_OFFENDERS.getSql(), firstSeqOnly), createParams(
                    "offenderNumbers", offenderNumbers, "latestOnly", latestOnly),
                MOVEMENT_MAPPER);
        }
        return jdbcTemplate.query(format(MovementsRepositorySql.GET_MOVEMENTS_BY_OFFENDERS_AND_MOVEMENT_TYPES.getSql(), firstSeqOnly), createParams(
                "offenderNumbers", offenderNumbers,
                "movementTypes", movementTypes,
                "latestOnly", latestOnly),
            MOVEMENT_MAPPER);
    }


    public List<OffenderMovement> getOffendersOut(final String agencyId, final LocalDate movementDate, final String movementType) {
        final var sql = MovementsRepositorySql.GET_OFFENDERS_OUT_TODAY.getSql();
        return jdbcTemplate.query(sql, createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(movementDate),
                "movementType", movementType),
            OFFENDER_MOVEMENT_MAPPER);
    }


    public List<RollCount> getRollCount(final String agencyId, final String certifiedFlag) {
        final var sql = MovementsRepositorySql.GET_ROLL_COUNT.getSql();
        return jdbcTemplate.query(sql, createParams(
                "agencyId", agencyId,
                "certifiedFlag", certifiedFlag,
                "livingUnitId", null,
                "deactivateReasonCodes", DEACTIVATE_REASON_CODES,
                "currentDateTime", new Date()),
            ROLLCOUNT_MAPPER);
    }


    public MovementCount getMovementCount(final String agencyId, final LocalDate date) {

        final var movements = jdbcTemplate.query(
            MovementsRepositorySql.GET_ROLLCOUNT_MOVEMENTS.getSql(),
            createParams("agencyId", agencyId, "movementDate", DateTimeConverter.toDate(date)), MOVEMENT_MAPPER);

        final var movementsGroupedByDirection = movements.stream().filter(movement ->
                (movement.getDirectionCode().equals("IN") && movement.getToAgency().equals(agencyId)) ||
                    (movement.getDirectionCode().equals("OUT") && movement.getFromAgency().equals(agencyId)))
            .collect(groupingBy(Movement::getDirectionCode));

        final var outMovements = movementsGroupedByDirection.containsKey("OUT") ? movementsGroupedByDirection.get("OUT").size() : 0;
        final var inMovements = movementsGroupedByDirection.containsKey("IN") ? movementsGroupedByDirection.get("IN").size() : 0;

        return MovementCount.builder()
            .out(outMovements)
            .in(inMovements)
            .build();
    }


    public List<OffenderMovement> getEnrouteMovementsOffenderMovementList(final String agencyId, final LocalDate date) {

        final var initialSql = MovementsRepositorySql.GET_ENROUTE_OFFENDER_MOVEMENTS.getSql();
        final var sql = date == null ? initialSql : initialSql + MOVEMENT_DATE_CLAUSE;

        return jdbcTemplate.query(sql,
            createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(date)),
            OFFENDER_MOVEMENT_MAPPER);
    }


    public int getEnrouteMovementsOffenderCount(final String agencyId, final LocalDate date) {

        return jdbcTemplate.queryForObject(
            MovementsRepositorySql.GET_ENROUTE_OFFENDER_COUNT.getSql(),
            createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(date)),
            Integer.class);
    }


    public List<OffenderIn> getOffendersIn(final String agencyId, final LocalDate movementDate) {
        return jdbcTemplate.query(MovementsRepositorySql.GET_OFFENDER_MOVEMENTS_IN.getSql(),
            createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(movementDate)),
            OFFENDER_IN_MAPPER);
    }


    public List<OffenderInReception> getOffendersInReception(final String agencyId) {
        return jdbcTemplate.query(MovementsRepositorySql.GET_OFFENDERS_IN_RECEPTION.getSql(),
            createParams("agencyId", agencyId),
            OFFENDER_IN_RECEPTION_MAPPER);
    }


    public List<OffenderOut> getOffendersCurrentlyOut(final long livingUnitId) {
        return jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDERS_CURRENTLY_OUT_OF_LIVING_UNIT.getSql(),
            createParams(
                "livingUnitId", livingUnitId,
                "bookingSeq", 1,
                "inOutStatus", "OUT"),
            OFFENDER_OUT_MAPPER);
    }


    public List<OffenderOut> getOffendersCurrentlyOut(final String agencyId) {
        return jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDERS_CURRENTLY_OUT_OF_AGENCY.getSql(),
            createParams(
                "agencyId", agencyId,
                "bookingSeq", 1,
                "inOutStatus", "OUT",
                "certifiedFlag", "Y",
                "activeFlag", "Y"
            ),
            OFFENDER_OUT_MAPPER);
    }


    public List<MovementSummary> getCompletedMovementsForAgencies(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
            MovementsRepositorySql.GET_MOVEMENTS_BY_AGENCY_AND_TIME_PERIOD.getSql(),
            createParams("agencyListFrom", agencies,
                "agencyListTo", agencies,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
            MOVEMENT_SUMMARY_MAPPER);
    }


    public List<CourtEvent> getCourtEvents(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
            MovementsRepositorySql.GET_COURT_EVENTS_BY_AGENCY_AND_TIME_PERIOD.getSql(),
            createParams("agencyListFrom", agencies,
                "agencyListTo", agencies,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
            COURT_EVENT_MAPPER);
    }


    public List<TransferEvent> getOffenderTransfers(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDER_TRANSFERS_BY_AGENCY_AND_TIME_PERIOD.getSql(),
            createParams("agencyListFrom", agencies,
                "agencyListTo", agencies,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
            OFFENDER_TRANSFER_MAPPER);
    }

    public List<TransferEvent> getIndividualSchedules(final LocalDate date) {

        return jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDER_INDIVIDUAL_SCHEDULES_BY_DATE.getSql(),
            createParams("date", DateTimeConverter.toDate(date)),
            OFFENDER_TRANSFER_MAPPER
        );
    }


    public List<ReleaseEvent> getOffenderReleases(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDER_RELEASES_BY_AGENCY_AND_DATE.getSql(),
            createParams("agencyListFrom", agencies,
                "fromDate", DateTimeConverter.fromTimestamp(DateTimeConverter.fromLocalDateTime(from)),
                "toDate", DateTimeConverter.fromTimestamp(DateTimeConverter.fromLocalDateTime(to))),
            OFFENDER_RELEASE_MAPPER);
    }

}
