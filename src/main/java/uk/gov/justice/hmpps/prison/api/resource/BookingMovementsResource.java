package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import uk.gov.justice.hmpps.prison.api.model.RequestMoveToCellSwap;
import uk.gov.justice.hmpps.prison.api.model.SchedulePrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.api.model.ScheduledPrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.CourtHearingCancellationService;
import uk.gov.justice.hmpps.prison.service.CourtHearingReschedulingService;
import uk.gov.justice.hmpps.prison.service.CourtHearingsService;
import uk.gov.justice.hmpps.prison.service.MovementUpdateService;
import uk.gov.justice.hmpps.prison.service.PrisonToPrisonMoveSchedulingService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@RestController
@Tag(name = "bookings")
@RequestMapping(value = "${api.base.path}/bookings", produces = "application/json")
@Validated
@Slf4j
@AllArgsConstructor
public class BookingMovementsResource {

    private final CourtHearingsService courtHearingsService;
    private final MovementUpdateService movementUpdateService;
    private final PrisonToPrisonMoveSchedulingService prisonToPrisonMoveSchedulingService;
    private final CourtHearingReschedulingService courtHearingReschedulingService;
    private final CourtHearingCancellationService courtHearingCancellationService;

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Court hearing created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtHearing.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Schedules a prison to court hearing for an offender and given court case.", description = "Schedules a prison to court hearing for an offender and given court case.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/court-cases/{courtCaseId}/prison-to-court-hearings")
    @ProxyUser
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER') and hasAuthority('SCOPE_write')")
    public CourtHearing prisonToCourt(@PathVariable("bookingId") @Parameter(description = "The offender booking to associate the court hearing with.", required = true) final Long bookingId,
                                      @PathVariable("courtCaseId") @Parameter(description = "The court case to associate the hearing with.", required = true) final Long courtCaseId,
                                      @RequestBody @Parameter(description = "The prison to court hearing to be scheduled for the offender booking.", required = true) final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, courtCaseId, hearing);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Court hearing created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtHearing.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Schedules a prison to court hearing for an offender.", description = "Schedules a prison to court hearing for an offender.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/prison-to-court-hearings")
    @ProxyUser
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER') and hasAuthority('SCOPE_write')")
    public CourtHearing prisonToCourt(@PathVariable("bookingId") @Parameter(description = "The offender booking to associate the court hearing with.", required = true) final Long bookingId,
                                      @RequestBody @Parameter(description = "The prison to court hearing to be scheduled for the offender booking.", required = true) final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, hearing);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtHearings.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("{bookingId}/court-hearings")
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "COURT_HEARING_MAINTAINER"})
    public CourtHearings getCourtHearings(@PathVariable("bookingId") @Parameter(description = "The offender booking linked to the court hearings.", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Return court hearings on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Return court hearings on or before this date (in YYYY-MM-DD format).") final LocalDate toDate) {
        return courtHearingsService.getCourtHearingsFor(bookingId, fromDate, toDate);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PutMapping("/{bookingId}/living-unit/{internalLocationDescription}")
    @ProxyUser
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "MAINTAIN_CELL_MOVEMENTS"})
    @HasWriteScope
    public CellMoveResult moveToCell(@PathVariable("bookingId") @Parameter(description = "The offender booking id", example = "1200866", required = true) final Long bookingId, @PathVariable("internalLocationDescription") @Parameter(description = "The cell location the offender has been moved to", example = "MDI-1-1", required = true) final String internalLocationDescription, @RequestParam("reasonCode") @Parameter(description = "The reason code for the move (from reason code domain CHG_HOUS_RSN)", example = "ADM", required = true) final String reasonCode, @RequestParam(value = "dateTime", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "The date / time of the move (defaults to current)", example = "2020-03-24T12:13:40") final LocalDateTime dateTime) {
        log.debug("Received moveToCell request for booking id {}, cell location {}, reasonCode {}, date/time {}",
                bookingId,
                internalLocationDescription,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCellOrReception(bookingId, internalLocationDescription, reasonCode, dateTime);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OffenderBooking.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PutMapping("/{bookingId}/move-to-cell-swap")
    @Operation(
        summary = "Move the prisoner from current cell to cell swap",
        description = "Using role MAINTAIN_CELL_MOVEMENTS will no longer check for user access to prisoner booking, this endpoint will be removed in future releases"
    )
    @ProxyUser
    @Deprecated
    @VerifyBookingAccess(overrideRoles = {"MAINTAIN_CELL_MOVEMENTS"})
    @HasWriteScope
    public CellMoveResult moveToCellSwap(@PathVariable("bookingId") @Parameter(description = "The offender booking id", example = "1200866", required = true) final Long bookingId, @RequestBody final RequestMoveToCellSwap requestMoveToCellSwap) {
        final var dateTime = requestMoveToCellSwap.getDateTime();
        final var reasonCode = requestMoveToCellSwap.getReasonCode();

        log.debug("Received moveToCellSwap request for booking id {}, cell location Cell swap, reasonCode {}, date/time {}",
                bookingId,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCellSwap(bookingId, reasonCode, dateTime);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The scheduled prison move.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledPrisonToPrisonMove.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Schedules a future prison to prison move for an offender.", description = "Schedules a future prison to prison move for an offender.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/prison-to-prison")
    @ProxyUser
    @PreAuthorize("hasRole('PRISON_MOVE_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ScheduledPrisonToPrisonMove prisonToPrison(@PathVariable("bookingId") @Parameter(description = "The offender booking to associate the prison to prison move with.", required = true) final Long bookingId, @RequestBody @Parameter(description = "The prison to prison move to be scheduled for the offender booking.", required = true) final @Valid SchedulePrisonToPrisonMove prisonMove) {
        return prisonToPrisonMoveSchedulingService.schedule(bookingId, prisonMove);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Cancels a scheduled prison to prison move for an offender.", description = "Cancels a scheduled prison to prison move for an offender.")
    @PutMapping("/{bookingId}/prison-to-prison/{eventId}/cancel")
    @ProxyUser
    @PreAuthorize("hasRole('PRISON_MOVE_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> cancelPrisonToPrisonMove(@PathVariable("bookingId") @Parameter(description = "The offender booking linked to the scheduled event.", required = true) final Long bookingId, @PathVariable("eventId") @Parameter(description = "The identifier of the scheduled event to be cancelled.", required = true) final Long eventId, @RequestBody @Parameter(description = "The cancellation details.", required = true) @Valid final PrisonMoveCancellation cancellation) {
        prisonToPrisonMoveSchedulingService.cancel(bookingId, eventId, cancellation.getReasonCode());

        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Amends the scheduled court hearing date and/or time for an offender.", description = "Amends the scheduled court hearing date and/or time for an offender.")
    @PutMapping("/{bookingId}/court-hearings/{hearingId}/hearing-date")
    @ProxyUser
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER') and hasAuthority('SCOPE_write')")
    public CourtHearing courtHearingDateAmendment(@PathVariable("bookingId") @Parameter(description = "The offender booking to associate the update with.", required = true) final Long bookingId, @PathVariable @Parameter(description = "The  court hearing to be updated.", required = true) final Long hearingId, @RequestBody @Parameter(description = "The amendments for the scheduled court hearing.", required = true) @Valid CourtHearingDateAmendment amendment) {
        return courtHearingReschedulingService.reschedule(bookingId, hearingId, amendment.getHearingDateTime());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Cancels the scheduled court hearing for an offender.", description = "Cancels the scheduled court hearing for an offender.")
    @DeleteMapping("/{bookingId}/court-hearings/{hearingId}/cancel")
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> cancelCourtHearing(@PathVariable("bookingId") @Parameter(description = "The offender booking to linked to the scheduled event.", required = true) final Long bookingId, @PathVariable("hearingId") @Parameter(description = "The identifier of the scheduled event to be cancelled.", required = true) final Long hearingId) {
        courtHearingCancellationService.cancel(bookingId, hearingId);

        return ResponseEntity.ok().build();
    }
}
