package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.CellMoveResult;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.api.model.CourtHearingDateAmendment;
import uk.gov.justice.hmpps.prison.api.model.CourtHearings;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.model.PrisonMoveCancellation;
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing;
import uk.gov.justice.hmpps.prison.api.model.PrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.api.model.RequestMoveToCellSwap;
import uk.gov.justice.hmpps.prison.api.model.ScheduledPrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.CourtHearingCancellationService;
import uk.gov.justice.hmpps.prison.service.CourtHearingReschedulingService;
import uk.gov.justice.hmpps.prison.service.CourtHearingsService;
import uk.gov.justice.hmpps.prison.service.MovementUpdateService;
import uk.gov.justice.hmpps.prison.service.PrisonToPrisonMoveSchedulingService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@RestController
@Api(tags = {"bookings"})
@RequestMapping("${api.base.path}/bookings")
@Validated
@Slf4j
public class OffenderMovementsResource {

    private final CourtHearingsService courtHearingsService;
    private final MovementUpdateService movementUpdateService;
    private final PrisonToPrisonMoveSchedulingService prisonToPrisonMoveSchedulingService;
    private final CourtHearingReschedulingService courtHearingReschedulingService;
    private final CourtHearingCancellationService courtHearingCancellationService;

    public OffenderMovementsResource(final CourtHearingsService courtHearingsService,
                                     final MovementUpdateService movementUpdateService,
                                     final PrisonToPrisonMoveSchedulingService prisonToPrisonMoveSchedulingService,
                                     final CourtHearingReschedulingService courtHearingReschedulingService,
                                     final CourtHearingCancellationService courtHearingCancellationService) {
        this.courtHearingsService = courtHearingsService;
        this.movementUpdateService = movementUpdateService;
        this.prisonToPrisonMoveSchedulingService = prisonToPrisonMoveSchedulingService;
        this.courtHearingReschedulingService = courtHearingReschedulingService;
        this.courtHearingCancellationService = courtHearingCancellationService;
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Court hearing created.", response = CourtHearing.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Schedules a prison to court hearing for an offender and given court case.", notes = "Schedules a prison to court hearing for an offender and given court case.", nickname = "prisonToCourt")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/court-cases/{courtCaseId}/prison-to-court-hearings")
    @ProxyUser
    public CourtHearing prisonToCourt(@PathVariable("bookingId") @ApiParam(value = "The offender booking to associate the court hearing with.", required = true) final Long bookingId, @PathVariable("courtCaseId") @ApiParam(value = "The court case to associate the hearing with.", required = true) final Long courtCaseId, @RequestBody @ApiParam(value = "The prison to court hearing to be scheduled for the offender booking.", required = true) final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, courtCaseId, hearing);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Court hearing created.", response = CourtHearing.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Schedules a prison to court hearing for an offender.", notes = "Schedules a prison to court hearing for an offender.", nickname = "prisonToCourt")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/prison-to-court-hearings")
    @ProxyUser
    public CourtHearing prisonToCourt(@PathVariable("bookingId") @ApiParam(value = "The offender booking to associate the court hearing with.", required = true) final Long bookingId, @RequestBody @ApiParam(value = "The prison to court hearing to be scheduled for the offender booking.", required = true) final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, hearing);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CourtHearings.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @GetMapping("{bookingId}/court-hearings")
    public CourtHearings getCourtHearings(@PathVariable("bookingId") @ApiParam(value = "The offender booking linked to the court hearings.", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Return court hearings on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Return court hearings on or before this date (in YYYY-MM-DD format).") final LocalDate toDate) {
        return courtHearingsService.getCourtHearingsFor(bookingId, fromDate, toDate);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @PutMapping("/{bookingId}/living-unit/{internalLocationDescription}")
    @ProxyUser
    public CellMoveResult moveToCell(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", example = "1200866", required = true) final Long bookingId, @PathVariable("internalLocationDescription") @ApiParam(value = "The cell location the offender has been moved to", example = "MDI-1-1", required = true) final String internalLocationDescription, @RequestParam("reasonCode") @ApiParam(value = "The reason code for the move (from reason code domain CHG_HOUS_RSN)", example = "ADM", required = true) final String reasonCode, @RequestParam(value = "dateTime", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "The date / time of the move (defaults to current)", example = "2020-03-24T12:13:40") final LocalDateTime dateTime) {
        log.debug("Received moveToCell request for booking id {}, cell location {}, reasonCode {}, date/time {}",
                bookingId,
                internalLocationDescription,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCell(bookingId, internalLocationDescription, reasonCode, dateTime);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @PutMapping("/{bookingId}/move-to-cell-swap")
    public CellMoveResult moveToCellSwap(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", example = "1200866", required = true) final Long bookingId, @RequestBody final RequestMoveToCellSwap requestMoveToCellSwap) {
        final var dateTime = requestMoveToCellSwap.getDateTime();
        final var reasonCode = requestMoveToCellSwap.getReasonCode();

        log.debug("Received moveToCellSwap request for booking id {}, cell location Cell swap, reasonCode {}, date/time {}",
                bookingId,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCellSwap(bookingId, reasonCode, dateTime);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "The scheduled prison move.", response = ScheduledPrisonToPrisonMove.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Schedules a future prison to prison move for an offender.", notes = "Schedules a future prison to prison move for an offender.", nickname = "prisonToPrison")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/prison-to-prison")
    @ProxyUser
    public ScheduledPrisonToPrisonMove prisonToPrison(@PathVariable("bookingId") @ApiParam(value = "The offender booking to associate the prison to prison move with.", required = true) final Long bookingId, @RequestBody @ApiParam(value = "The prison to prison move to be scheduled for the offender booking.", required = true) final @Valid PrisonToPrisonMove prisonMove) {
        return prisonToPrisonMoveSchedulingService.schedule(bookingId, prisonMove);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Cancels a scheduled prison to prison move for an offender.", notes = "Cancels a scheduled prison to prison move for an offender.", nickname = "cancelPrisonToPrisonMove")
    @PutMapping("/{bookingId}/prison-to-prison/{eventId}/cancel")
    @ProxyUser
    public ResponseEntity<Void> cancelPrisonToPrisonMove(@PathVariable("bookingId") @ApiParam(value = "The offender booking linked to the scheduled event.", required = true) final Long bookingId, @PathVariable("eventId") @ApiParam(value = "The identifier of the scheduled event to be cancelled.", required = true) final Long eventId, @RequestBody @ApiParam(value = "The cancellation details.", required = true) @Valid final PrisonMoveCancellation cancellation) {
        prisonToPrisonMoveSchedulingService.cancel(bookingId, eventId, cancellation.getReasonCode());

        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Amends the scheduled court hearing date and/or time for an offender.", notes = "Amends the scheduled court hearing date and/or time for an offender.", nickname = "courtHearingDateAmendment")
    @PutMapping("/{bookingId}/court-hearings/{hearingId}/hearing-date")
    @ProxyUser
    public CourtHearing courtHearingDateAmendment(@PathVariable("bookingId") @ApiParam(value = "The offender booking to associate the update with.", required = true) final Long bookingId, @PathVariable @ApiParam(value = "The  court hearing to be updated.", required = true) final Long hearingId, @RequestBody @ApiParam(value = "The amendments for the scheduled court hearing.", required = true) @Valid CourtHearingDateAmendment amendment) {
        return courtHearingReschedulingService.reschedule(bookingId, hearingId, amendment.getHearingDateTime());
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Cancels the scheduled court hearing for an offender.", notes = "Cancels the scheduled court hearing for an offender.", nickname = "cancelCourtHearing")
    @DeleteMapping("/{bookingId}/court-hearings/{hearingId}/cancel")
    public ResponseEntity<Void> cancelCourtHearing(@PathVariable("bookingId") @ApiParam(value = "The offender booking to linked to the scheduled event.", required = true) final Long bookingId, @PathVariable("hearingId") @ApiParam(value = "The identifier of the scheduled event to be cancelled.", required = true) final Long hearingId) {
        courtHearingCancellationService.cancel(bookingId, hearingId);

        return ResponseEntity.ok().build();
    }
}
