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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import uk.gov.justice.hmpps.prison.api.model.CourtHearings;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.CourtHearingsService;
import uk.gov.justice.hmpps.prison.service.MovementUpdateService;

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

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Court hearing created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtHearing.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Schedules a prison to court hearing for an offender and given court case.", description = "Schedules a prison to court hearing for an offender and given court case. Requires role COURT_HEARING_MAINTAINER and scope write")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/court-cases/{courtCaseId}/prison-to-court-hearings")
    @ProxyUser
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER') and hasAuthority('SCOPE_write')")
    public CourtHearing prisonToCourt(@PathVariable @Parameter(description = "The offender booking to associate the court hearing with.", required = true) final Long bookingId,
                                      @PathVariable @Parameter(description = "The court case to associate the hearing with.", required = true) final Long courtCaseId,
                                      @RequestBody @Parameter(description = "The prison to court hearing to be scheduled for the offender booking.", required = true) final @Valid PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, courtCaseId, hearing);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtHearings.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/{bookingId}/court-hearings")
    @VerifyBookingAccess(overrideRoles = {"COURT_HEARING_MAINTAINER"})
    public CourtHearings getCourtHearings(
        @PathVariable @Parameter(description = "The offender booking linked to the court hearings.", required = true) final Long bookingId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Return court hearings on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Return court hearings on or before this date (in YYYY-MM-DD format).") final LocalDate toDate
    ) {
        return courtHearingsService.getCourtHearingsFor(bookingId, fromDate, toDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Record in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(
        summary = "Move Offender to another cell, reception or cell swap",
        description = "Pass the destination's description. For a cell swap this is the prison's CSWAP location, described PRISON_ID-CSWAP - for example LEI-CSWAP. A cell swap destination is exempt from the capacity check, since cell swap is deliberately uncapped."
    )
    @PutMapping("/{bookingId}/living-unit/{internalLocationDescription}")
    @ProxyUser
    @VerifyBookingAccess(overrideRoles = {"MAINTAIN_CELL_MOVEMENTS"})
    @HasWriteScope
    public CellMoveResult moveToCell(
        @PathVariable @Parameter(description = "The offender booking id", example = "1200866", required = true) final Long bookingId,
        @PathVariable @Parameter(description = "The cell location the offender has been moved to", example = "MDI-1-1", required = true) final String internalLocationDescription,
        @RequestParam("reasonCode") @Parameter(description = "The reason code for the move (from reason code domain CHG_HOUS_RSN)", example = "ADM", required = true) final String reasonCode,
        @RequestParam(value = "dateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "The date / time of the move (defaults to current)", example = "2020-03-24T12:13:40") final LocalDateTime dateTime,
        @RequestParam(value = "lockTimeout", required = false, defaultValue = "false") @Parameter(description = "Whether to timeout if locked", example = "true") final Boolean lockTimeout
    ) {
        log.debug("Received moveToCell request for booking id {}, cell location {}, reasonCode {}, date/time {}",
            bookingId,
            internalLocationDescription,
            reasonCode,
            dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCellOrReception(bookingId, internalLocationDescription, reasonCode, dateTime, lockTimeout);
    }
}
