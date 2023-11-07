package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.CourtEventDto;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.MovementDto;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.MovementSummaryDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReceptionDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovementDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutDto;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEventDto;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.RollCountDto;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;
import uk.gov.justice.hmpps.prison.api.model.TransferEventDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.MovementsRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

@Repository
public class MovementsRepository extends RepositoryBase {

    private static final Set<String> DEACTIVATE_REASON_CODES = Set.of("A", "C", "E", "I");
    private final RowMapper<MovementDto> MOVEMENT_MAPPER = new DataClassByColumnRowMapper<>(MovementDto.class);
    private final RowMapper<OffenderMovementDto> OFFENDER_MOVEMENT_MAPPER = new DataClassByColumnRowMapper<>(OffenderMovementDto.class);
    private final RowMapper<RollCountDto> ROLLCOUNT_MAPPER = new DataClassByColumnRowMapper<>(RollCountDto.class);
    private final RowMapper<OffenderInDto> OFFENDER_IN_MAPPER = new DataClassByColumnRowMapper<>(OffenderInDto.class);
    private final RowMapper<OffenderOutDto> OFFENDER_OUT_MAPPER = new DataClassByColumnRowMapper<>(OffenderOutDto.class);
    private final RowMapper<OffenderInReceptionDto> OFFENDER_IN_RECEPTION_MAPPER = new DataClassByColumnRowMapper<>(OffenderInReceptionDto.class);
    private final RowMapper<MovementSummaryDto> MOVEMENT_SUMMARY_MAPPER = new DataClassByColumnRowMapper<>(MovementSummaryDto.class);
    private final RowMapper<CourtEventDto> COURT_EVENT_MAPPER = new DataClassByColumnRowMapper<>(CourtEventDto.class);
    private final RowMapper<TransferEventDto> OFFENDER_TRANSFER_MAPPER = new DataClassRowMapper<>(TransferEventDto.class);
    private final RowMapper<ReleaseEventDto> OFFENDER_RELEASE_MAPPER = new DataClassByColumnRowMapper<>(ReleaseEventDto.class);

    private static final String MOVEMENT_DATE_CLAUSE = " AND OEM.MOVEMENT_DATE = :movementDate";


    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final List<String> movementTypes) {
        final var sql = MovementsRepositorySql.GET_RECENT_MOVEMENTS_BY_DATE_FOR_BATCH.getSql();
        final var types = (movementTypes == null || movementTypes.isEmpty()) ? Set.of("TRN", "REL", "ADM") : movementTypes;

        final var movements = jdbcTemplate.query(sql,
            createParams(
                "movementTypes", types,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(fromDateTime),
                "movementDate", DateTimeConverter.toDate(movementDate)), MOVEMENT_MAPPER);
        return movements.stream().map(MovementDto::toMovement).collect(Collectors.toList());
    }

    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final boolean latestOnly, final boolean allBookings) {
        final var firstSeqOnly = allBookings ? "" : "AND OB.BOOKING_SEQ = 1";
        final List<MovementDto> movements;
        if (movementTypes == null || movementTypes.isEmpty()) {
            movements = jdbcTemplate.query(format(MovementsRepositorySql.GET_MOVEMENTS_BY_OFFENDERS.getSql(), firstSeqOnly), createParams(
                    "offenderNumbers", offenderNumbers, "latestOnly", latestOnly),
                MOVEMENT_MAPPER);
        } else {
            movements = jdbcTemplate.query(format(MovementsRepositorySql.GET_MOVEMENTS_BY_OFFENDERS_AND_MOVEMENT_TYPES.getSql(), firstSeqOnly), createParams(
                    "offenderNumbers", offenderNumbers,
                    "movementTypes", movementTypes,
                    "latestOnly", latestOnly),
                MOVEMENT_MAPPER);
        }
        return movements.stream().map(MovementDto::toMovement).collect(Collectors.toList());
    }


    public List<OffenderMovement> getOffendersOut(final String agencyId, final LocalDate movementDate, final String movementType) {
        final var sql = MovementsRepositorySql.GET_OFFENDERS_OUT_TODAY.getSql();
        final var movements = jdbcTemplate.query(sql, createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(movementDate),
                "movementType", movementType),
            OFFENDER_MOVEMENT_MAPPER);
        return movements.stream().map(OffenderMovementDto::toOffenderMovement).collect(Collectors.toList());
    }


    public List<RollCount> getRollCount(final String agencyId, final String certifiedFlag) {
        final var sql = MovementsRepositorySql.GET_ROLL_COUNT.getSql();
        final var rollcounts = jdbcTemplate.query(sql, createParams(
                "agencyId", agencyId,
                "certifiedFlag", certifiedFlag,
                "livingUnitId", null,
                "deactivateReasonCodes", DEACTIVATE_REASON_CODES,
                "currentDateTime", new Date()),
            ROLLCOUNT_MAPPER);
        return rollcounts.stream().map(RollCountDto::toRollCount).collect(Collectors.toList());
    }


    public MovementCount getMovementCount(final String agencyId, final LocalDate date) {

        final var movements = jdbcTemplate.query(
            MovementsRepositorySql.GET_ROLLCOUNT_MOVEMENTS.getSql(),
            createParams("agencyId", agencyId, "movementDate", DateTimeConverter.toDate(date)), MOVEMENT_MAPPER);

        final var movementsGroupedByDirection = movements.stream()
            .filter(movement ->
                (movement.getDirectionCode().equals("IN") && movement.getToAgency().equals(agencyId)) ||
                    (movement.getDirectionCode().equals("OUT") && movement.getFromAgency().equals(agencyId)))
            .map(MovementDto::toMovement)
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

        final var movements = jdbcTemplate.query(sql,
            createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(date)),
            OFFENDER_MOVEMENT_MAPPER);
        return movements.stream().map(OffenderMovementDto::toOffenderMovement).collect(Collectors.toList());
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
        final var offenders = jdbcTemplate.query(MovementsRepositorySql.GET_OFFENDER_MOVEMENTS_IN.getSql(),
            createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(movementDate)),
            OFFENDER_IN_MAPPER);
        return offenders.stream().map(OffenderInDto::toOffenderIn).collect(Collectors.toList());
    }


    public List<OffenderInReception> getOffendersInReception(final String agencyId) {
        final var offenders = jdbcTemplate.query(MovementsRepositorySql.GET_OFFENDERS_IN_RECEPTION.getSql(),
            createParams("agencyId", agencyId),
            OFFENDER_IN_RECEPTION_MAPPER);
        return offenders.stream().map(OffenderInReceptionDto::toOffenderInReception).collect(Collectors.toList());
    }


    public List<OffenderOut> getOffendersCurrentlyOut(final long livingUnitId) {
        final var offenders = jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDERS_CURRENTLY_OUT_OF_LIVING_UNIT.getSql(),
            createParams(
                "livingUnitId", livingUnitId,
                "bookingSeq", 1,
                "inOutStatus", "OUT"),
            OFFENDER_OUT_MAPPER);
        return offenders.stream().map(OffenderOutDto::toOffenderOut).collect(Collectors.toList());
    }


    public List<OffenderOut> getOffendersCurrentlyOut(final String agencyId) {
        final var offenders = jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDERS_CURRENTLY_OUT_OF_AGENCY.getSql(),
            createParams(
                "agencyId", agencyId,
                "bookingSeq", 1,
                "inOutStatus", "OUT",
                "certifiedFlag", "Y",
                "activeFlag", "Y"
            ),
            OFFENDER_OUT_MAPPER);
        return offenders.stream().map(OffenderOutDto::toOffenderOut).collect(Collectors.toList());
    }


    public List<MovementSummary> getCompletedMovementsForAgencies(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        final var movements = jdbcTemplate.query(
            MovementsRepositorySql.GET_MOVEMENTS_BY_AGENCY_AND_TIME_PERIOD.getSql(),
            createParams("agencyListFrom", agencies,
                "agencyListTo", agencies,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
            MOVEMENT_SUMMARY_MAPPER);
        return movements.stream().map(MovementSummaryDto::toMovementSummary).collect(Collectors.toList());
    }


    public List<CourtEvent> getCourtEvents(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        final var courts = jdbcTemplate.query(
            MovementsRepositorySql.GET_COURT_EVENTS_BY_AGENCY_AND_TIME_PERIOD.getSql(),
            createParams("agencyListFrom", agencies,
                "agencyListTo", agencies,
                "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
            COURT_EVENT_MAPPER);
        return courts.stream().map(CourtEventDto::toCourtEvent).collect(Collectors.toList());
    }

    public List<TransferEvent> getIndividualSchedules(final List<String> agencies, final LocalDate date) {
        final var transfers = jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDER_INDIVIDUAL_SCHEDULES_BY_DATE.getSql(),
            createParams("date", DateTimeConverter.toDate(date), "agencyListFrom", agencies, "agencyListTo", agencies),
            OFFENDER_TRANSFER_MAPPER
        );
        return transfers.stream().map(TransferEventDto::toTransferEvent).collect(Collectors.toList());
    }

    public List<ReleaseEvent> getOffenderReleases(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        final var releases = jdbcTemplate.query(
            MovementsRepositorySql.GET_OFFENDER_RELEASES_BY_AGENCY_AND_DATE.getSql(),
            createParams("agencyListFrom", agencies,
                "fromDate", DateTimeConverter.fromTimestamp(DateTimeConverter.fromLocalDateTime(from)),
                "toDate", DateTimeConverter.fromTimestamp(DateTimeConverter.fromLocalDateTime(to))),
            OFFENDER_RELEASE_MAPPER);
        return releases.stream().map(ReleaseEventDto::toReleaseEvent).collect(Collectors.toList());
    }

}
