package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferSummary;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Api(tags = {"movements"})
@Validated
@RequestMapping("${api.base.path}/movements")
public class MovementResource {

    private final MovementsService movementsService;

    public MovementResource(final MovementsService movementsService) {
        this.movementsService = movementsService;
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Returns a list of recently released or moved offender nos and the associated timestamp.", notes = "Returns a list of recently released or moved offender nos and the associated timestamp.", nickname = "getRecentMovementsByDate")
    @GetMapping
    @SuppressWarnings("RestParamTypeInspection")
    public List<Movement> getRecentMovementsByDate(@RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "A timestamp that indicates the earliest record required", required = true) final LocalDateTime fromDateTime, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("The date for which movements are searched, defaults to today") final LocalDate movementDate, @RequestParam(value = "agencyId", required = false) @ApiParam("Filter to just movements to or from this agency.") final String agencyId, @RequestParam(value = "movementTypes", required = false) @ApiParam("movement type codes to filter by, defaults to TRN, REL, ADM") final List<String> movementTypes) {
        return movementsService.getRecentMovementsByDate(fromDateTime, movementDate, movementTypes);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Current establishment rollcount numbers.", notes = "Current establishment rollcount numbers.", nickname = "getRollcount")
    @GetMapping("/rollcount/{agencyId}")
    public List<RollCount> getRollcount(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId, @RequestParam(value = "unassigned", required = false, defaultValue = "false") @ApiParam(value = "If false return data for prisoners in cell locations, if true return unassigned prisoners, i.e. those in non-cell locations.", defaultValue = "false") final boolean unassigned) {
        return movementsService.getRollCount(agencyId, unassigned);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Rollcount movement numbers.", notes = "Rollcount movement numbers.", nickname = "getRollcountMovements")
    @GetMapping("/rollcount/{agencyId}/movements")
    public MovementCount getRollcountMovements(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("The date for which movements are counted, default today.") final LocalDate movementDate) {
        return movementsService.getMovementCount(agencyId, movementDate);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Information on offenders in today.", notes = "Information on offenders in on given date.", nickname = "getMovementsInOnDate")
    @GetMapping("/{agencyId}/in/{isoDate}")
    public List<OffenderIn> getMovementsIn(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId, @PathVariable("isoDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "date", required = true) final LocalDate date) {
        return movementsService.getOffendersIn(agencyId, date);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offenders who entered a prison during a time period.", notes = "Offenders who entered a prison during a time period.", nickname = "getMovementsInBetweeen")
    @GetMapping("/{agencyId}/in")
    public ResponseEntity<List<OffenderIn>> getMovementsIn(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId,
                                                           @RequestParam(value = "allMovements", required = false, defaultValue = "false") @ApiParam(value = "Returns movements for inactive prisoners", defaultValue = "false") final boolean allMovements,
                                                           @RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "fromDateTime", required = true) final LocalDateTime fromDate,
                                                           @RequestParam(value = "toDateTime", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "toDateTime") final LocalDateTime toDate,
                                                           @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of prisoner records.", defaultValue = "0") final Long pageOffset,
                                                           @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of records returned.", defaultValue = "10") final Long pageLimit) {
        final var page = movementsService.getOffendersIn(agencyId, fromDate, toDate, PageRequest.of(pageOffset, pageLimit), allMovements);

        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(page.getTotalElements()));
        responseHeaders.set("Page-Offset", String.valueOf(page.getPageable().getOffset()));
        responseHeaders.set("Page-Limit", String.valueOf(page.getPageable().getPageSize()));

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .body(page.getContent());
    }

    @ApiOperation(value = "", nickname = "getMovementsByOffenders")
    @PostMapping("/offenders")
    public List<Movement> getMovementsByOffenders(@RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderNumbers,
                                                  @RequestParam(value = "movementTypes", required = false) @ApiParam("movement type codes to filter by") final List<String> movementTypes,
                                                  @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only latest movement for the offenders specified", defaultValue = "true") final Boolean latestOnly,
                                                  @RequestParam(value = "allBookings", required = false, defaultValue = "false") @ApiParam(value = "Returns all movements for this offender list from all bookings if true", defaultValue = "false") final boolean allBookings) {
        return movementsService.getMovementsByOffenders(offenderNumbers, movementTypes, latestOnly == null || latestOnly, allBookings);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Enroute prisoner movement details.", notes = "Enroute to reception", nickname = "getEnrouteOffenderMovements")
    @GetMapping("/{agencyId}/enroute")
    public List<OffenderMovement> getEnrouteOffenderMovements(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Optional filter on date of movement") final LocalDate movementDate) {
        return movementsService.getEnrouteOffenderMovements(agencyId, movementDate);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Enroute prisoner movement count.", notes = "Enroute to reception count", nickname = "getEnrouteOffenderMovementCount")
    @GetMapping("/rollcount/{agencyId}/enroute")
    public int getEnrouteOffenderMovementCount(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId, @RequestParam(value = "movementDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Optional filter on date of movement. Defaults to today") final LocalDate movementDate) {
        return movementsService.getEnrouteOffenderCount(agencyId, movementDate);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "", nickname = "getOffendersOut")
    @GetMapping("/{agencyId}/out/{isoDate}")
    public List<OffenderOutTodayDto> getOffendersOutToday(
        @PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId,
        @PathVariable("isoDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "date", required = true) final LocalDate movementsDate,
        @RequestParam(value = "movementType", required = false) @ApiParam(value = "The optional movement type to filter by e.g CRT, REL, TAP, TRN") final String movementType
    ) {
        return movementsService.getOffendersOut(agencyId, movementsDate, movementType);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "", nickname = "getOffendersInReception")
    @GetMapping("/rollcount/{agencyId}/in-reception")
    public List<OffenderInReception> getOffendersInReception(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId) {
        return movementsService.getOffendersInReception(agencyId);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Information on offenders currently out.", notes = "Information on offenders currently out.", nickname = "getOffendersCurrentlyOut")
    @GetMapping("/livingUnit/{livingUnitId}/currently-out")
    public List<OffenderOut> getOffendersCurrentlyOut(@PathVariable("livingUnitId") @ApiParam(value = "The identifier of a living unit, otherwise known as an internal location.", required = true) final Long livingUnitId) {
        return movementsService.getOffendersCurrentlyOut(livingUnitId);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Information on offenders currently out.", notes = "Information on offenders currently out.", nickname = "getOffendersCurrentlyOut")
    @GetMapping("/agency/{agencyId}/currently-out")
    public List<OffenderOut> getOffendersCurrentlyOut(@PathVariable("agencyId") @ApiParam(value = "The prison id", required = true) final String agencyId) {
        return movementsService.getOffendersCurrentlyOut(agencyId);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid agency identifiers, or from time after the to time, or a time period greater than 24 hours specified, or parameter format not correct.", response = ErrorResponse.class),
        @ApiResponse(code = 401, message = "The token presented did not contain the necessary role to access this resource.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "The token presented has expired.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Information on scheduled court, transfer and release events, and confirmed movements between two dates/times for a specified number of agencies.", notes = "Planned movements are recorded as events of type court, release or transfers/appointments. When these events are started they are actualised as external movements.", nickname = "getTransfers")
    @GetMapping("/transfers")
    public TransferSummary getTransfers(@RequestParam("agencyId") @NotEmpty @ApiParam(value = "One or more agencyId values eg.agencyId=LEI&agencyId=MDI", required = true) final List<String> agencyIds,
                                        @RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "From date and time ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true) final LocalDateTime fromDateTime, @RequestParam("toDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "To date and time in ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true) final LocalDateTime toDateTime,
                                        @RequestParam(value = "courtEvents", required = false, defaultValue = "false") @ApiParam(value = "Set to true to include planned court events", required = false, defaultValue = "false") final boolean courtEvents, @RequestParam(value = "releaseEvents", required = false, defaultValue = "false") @ApiParam(value = "Set to true to include planned release events", required = false, defaultValue = "false") final boolean releaseEvents,
                                        @RequestParam(value = "transferEvents", required = false, defaultValue = "false") @ApiParam(value = "Set to true to include planned transfer/appointment events", required = false, defaultValue = "false") final boolean transferEvents, @RequestParam(value = "movements", required = false, defaultValue = "false") @ApiParam(value = "Set to true to include confirmed movements", required = false, defaultValue = "false") final boolean movements) {
        return movementsService.getTransferMovementsForAgencies(agencyIds, fromDateTime, toDateTime, courtEvents, releaseEvents, transferEvents, movements);
    }

    @ApiOperation(value = "Get future court hearings for all offenders", nickname = "getUpcomingCourtAppearances")
    @GetMapping("/upcomingCourtAppearances")
    public List<CourtEventBasic> getUpcomingCourtAppearances() {
        return movementsService.getUpcomingCourtAppearances();
    }

    @ApiOperation(value = "Create a new external movement", nickname = "createExternalMovement")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)}
    )
    @PostMapping
    public OffenderMovement createExternalMovement(@Valid @RequestBody CreateExternalMovement createExternalMovement) {
        return movementsService.createExternalMovement(createExternalMovement.getBookingId(), createExternalMovement);
    }
}
