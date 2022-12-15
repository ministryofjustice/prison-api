package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.CourtEventBasic;
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto;
import uk.gov.justice.hmpps.prison.api.model.OutOnTemporaryAbsenceSummary;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferSummary;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "movements")
@Validated
@RequestMapping(value = "${api.base.path}/movements", produces = "application/json")
public class MovementResource {

    private final MovementsService movementsService;

    public MovementResource(final MovementsService movementsService) {
        this.movementsService = movementsService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns a list of recently released or moved offender nos and the associated timestamp.", description = "Returns a list of recently released or moved offender nos and the associated timestamp.")
    @GetMapping
    public List<Movement> getRecentMovementsByDate(@RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "A timestamp that indicates the earliest record required", required = true) final LocalDateTime fromDateTime,
                                                   @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "The date for which movements are searched, defaults to today") final LocalDate movementDate,
                                                   @RequestParam(value = "movementTypes", required = false) @Parameter(description = "movement type codes to filter by, defaults to TRN, REL, ADM") final List<String> movementTypes) {
        return movementsService.getRecentMovementsByDate(fromDateTime, movementDate, movementTypes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Current establishment rollcount numbers.", description = "Current establishment rollcount numbers.")
    @GetMapping("/rollcount/{agencyId}")
    @SlowReportQuery
    public List<RollCount> getRollcount(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId, @RequestParam(value = "unassigned", required = false, defaultValue = "false") @Parameter(description = "If false return data for prisoners in cell locations, if true return unassigned prisoners, i.e. those in non-cell locations.") final boolean unassigned) {
        return movementsService.getRollCount(agencyId, unassigned);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Rollcount movement numbers.", description = "Rollcount movement numbers.")
    @GetMapping("/rollcount/{agencyId}/movements")
    @SlowReportQuery
    public MovementCount getRollcountMovements(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "The date for which movements are counted, default today.") final LocalDate movementDate) {
        return movementsService.getMovementCount(agencyId, movementDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Information on offenders in today.", description = "Information on offenders in on given date.")
    @GetMapping("/{agencyId}/in/{isoDate}")
    public List<OffenderIn> getMovementsIn(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId, @PathVariable("isoDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "date", required = true) final LocalDate date) {
        return movementsService.getOffendersIn(agencyId, date);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offenders who entered a prison during a time period.", description = "Offenders who entered a prison during a time period.")
    @GetMapping("/{agencyId}/in")
    @SlowReportQuery
    public ResponseEntity<List<OffenderIn>> getMovementsIn(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId,
                                                           @RequestParam(value = "allMovements", required = false, defaultValue = "false") @Parameter(description = "Returns movements for inactive prisoners") final boolean allMovements,
                                                           @RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "fromDateTime", required = true) final LocalDateTime fromDate,
                                                           @RequestParam(value = "toDateTime", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "toDateTime") final LocalDateTime toDate,
                                                           @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of prisoner records.") final Long pageOffset,
                                                           @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of records returned.") final Long pageLimit) {
        final var page = movementsService.getOffendersIn(agencyId, fromDate, toDate, PageRequest.of(pageOffset, pageLimit), allMovements);

        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(page.getTotalElements()));
        responseHeaders.set("Page-Offset", String.valueOf(page.getPageable().getOffset()));
        responseHeaders.set("Page-Limit", String.valueOf(page.getPageable().getPageSize()));

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .body(page.getContent());
    }

    @Operation
    @PostMapping("/offenders")
    public List<Movement> getMovementsByOffenders(@RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderNumbers,
                                                  @RequestParam(value = "movementTypes", required = false) @Parameter(description = "movement type codes to filter by") final List<String> movementTypes,
                                                  @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @Parameter(description = "Returns only latest movement for the offenders specified") final Boolean latestOnly,
                                                  @RequestParam(value = "allBookings", required = false, defaultValue = "false") @Parameter(description = "Returns all movements for this offender list from all bookings if true") final boolean allBookings) {
        return movementsService.getMovementsByOffenders(offenderNumbers, movementTypes, latestOnly == null || latestOnly, allBookings);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Enroute prisoner movement details.", description = "Enroute to reception")
    @GetMapping("/{agencyId}/enroute")
    public List<OffenderMovement> getEnrouteOffenderMovements(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Optional filter on date of movement") final LocalDate movementDate) {
        return movementsService.getEnrouteOffenderMovements(agencyId, movementDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Enroute prisoner movement count.", description = "Enroute to reception count")
    @GetMapping("/rollcount/{agencyId}/enroute")
    public int getEnrouteOffenderMovementCount(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Optional filter on date of movement. Defaults to today") final LocalDate movementDate) {
        return movementsService.getEnrouteOffenderCount(agencyId, movementDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation
    @GetMapping("/{agencyId}/out/{isoDate}")
    public List<OffenderOutTodayDto> getOffendersOutToday(
        @PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId,
        @PathVariable("isoDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "date", required = true) final LocalDate movementsDate,
        @RequestParam(value = "movementType", required = false) @Parameter(description = "The optional movement type to filter by e.g CRT, REL, TAP, TRN") final String movementType
    ) {
        return movementsService.getOffendersOut(agencyId, movementsDate, movementType);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation
    @GetMapping("/rollcount/{agencyId}/in-reception")
    public List<OffenderInReception> getOffendersInReception(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId) {
        return movementsService.getOffendersInReception(agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Information on offenders currently out.", description = "Information on offenders currently out.")
    @GetMapping("/livingUnit/{livingUnitId}/currently-out")
    @SlowReportQuery
    public List<OffenderOut> getOffendersCurrentlyOut(@PathVariable("livingUnitId") @Parameter(description = "The identifier of a living unit, otherwise known as an internal location.", required = true) final Long livingUnitId) {
        return movementsService.getOffendersCurrentlyOut(livingUnitId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Information on offenders currently out.", description = "Information on offenders currently out.")
    @GetMapping("/agency/{agencyId}/currently-out")
    public List<OffenderOut> getOffendersCurrentlyOut(@PathVariable("agencyId") @Parameter(description = "The prison id", required = true) final String agencyId) {
        return movementsService.getOffendersCurrentlyOut(agencyId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid agency identifiers, or from time after the to time, or a time period greater than 24 hours specified, or parameter format not correct.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "The token presented did not contain the necessary role to access this resource.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "The token presented has expired.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Information on scheduled court, transfer and release events, and confirmed movements between two dates/times for a specified number of agencies.", description = "Planned movements are recorded as events of type court, release or transfers/appointments. When these events are started they are actualised as external movements.")
    @GetMapping("/transfers")
    @SlowReportQuery
    public TransferSummary getTransfers(@RequestParam("agencyId") @NotEmpty @Parameter(description = "One or more agencyId values eg.agencyId=LEI&agencyId=MDI", required = true) final List<String> agencyIds,
                                        @RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "From date and time ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true) final LocalDateTime fromDateTime, @RequestParam("toDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "To date and time in ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true) final LocalDateTime toDateTime,
                                        @RequestParam(value = "courtEvents", required = false, defaultValue = "false") @Parameter(description = "Set to true to include planned court events") final boolean courtEvents, @RequestParam(value = "releaseEvents", required = false, defaultValue = "false") @Parameter(description = "Set to true to include planned release events") final boolean releaseEvents,
                                        @RequestParam(value = "transferEvents", required = false, defaultValue = "false") @Parameter(description = "Set to true to include planned transfer/appointment events") final boolean transferEvents, @RequestParam(value = "movements", required = false, defaultValue = "false") @Parameter(description = "Set to true to include confirmed movements") final boolean movements) {
        return movementsService.getTransferMovementsForAgencies(agencyIds, fromDateTime, toDateTime, courtEvents, releaseEvents, transferEvents, movements);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "The token presented did not contain the necessary role to access this resource.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "The token presented has expired.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Information about the set of offenders at an agency who are currently out due to temporary absence.")
    @GetMapping("/agency/{agencyId}/temporary-absences")
    public List<OutOnTemporaryAbsenceSummary> getTemporaryAbsences(
        @PathVariable("agencyId")
        @Parameter(description = "The prison id", required = true)
        final String agencyId) {
        return movementsService.getOffendersOutOnTemporaryAbsence(agencyId);
    }



    @Operation(summary = "Get future court hearings for all offenders")
    @GetMapping("/upcomingCourtAppearances")
    @SlowReportQuery
    public List<CourtEventBasic> getUpcomingCourtAppearances() {
        return movementsService.getUpcomingCourtAppearances();
    }

    @Operation(summary = "Create a new external movement for inactive bookings only, requires the INACTIVE_BOOKINGS role")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})}
    )
    @PostMapping
    @PreAuthorize("hasRole('INACTIVE_BOOKINGS') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public OffenderMovement createExternalMovement(@Valid @RequestBody CreateExternalMovement createExternalMovement) {
        return movementsService.createExternalMovement(createExternalMovement.getBookingId(), createExternalMovement);
    }
}
