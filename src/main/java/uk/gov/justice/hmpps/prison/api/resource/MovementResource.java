package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.hmpps.prison.api.model.CourtEventBasic;
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

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {"/movements"}, description = "Offender Movement Information")
@SuppressWarnings("unused")
public interface MovementResource {

    @SuppressWarnings("RestParamTypeInspection")
    @GetMapping
    @ApiOperation(value = "Returns a list of recently released or moved offender nos and the associated timestamp.", notes = "Returns a list of recently released or moved offender nos and the associated timestamp.", nickname = "getRecentMovementsByDate")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Movement> getRecentMovementsByDate(
            @ApiParam(value = "A timestamp that indicates the earliest record required", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDateTime") LocalDateTime fromDateTime,
            @ApiParam(value = "The date for which movements are searched, defaults to today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "movementDate", required = false) LocalDate movementDate,
            @ApiParam(value = "Filter to just movements to or from this agency.") @RequestParam(value = "agencyId", required = false) String agencyId,
            @ApiParam(value = "movement type codes to filter by, defaults to TRN, REL, ADM") @RequestParam(value = "movementTypes", required = false) List<String> movementTypes);

    @GetMapping("/rollcount/{agencyId}")
    @ApiOperation(value = "Current establishment rollcount numbers.", notes = "Current establishment rollcount numbers.", nickname = "getRollcount")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<RollCount> getRollcount(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId,
            @ApiParam(value = "If false return data for prisoners in cell locations, if true return unassigned prisoners, i.e. those in non-cell locations.", defaultValue = "false") @RequestParam(value = "unassigned", required = false, defaultValue = "false") boolean unassigned);

    @GetMapping("/rollcount/{agencyId}/movements")
    @ApiOperation(value = "Rollcount movement numbers.", notes = "Rollcount movement numbers.", nickname = "getRollcountMovements")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    MovementCount getRollcountMovements(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId,
            @ApiParam(value = "The date for which movements are counted, default today.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "movementDate", required = false) LocalDate movementDate);

    @PostMapping("/offenders")
    @ApiOperation(value = "", nickname = "getMovementsByOffenders")
    List<Movement> getMovementsByOffenders(
            @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
            @ApiParam(value = "movement type codes to filter by") @RequestParam(value = "movementTypes", required = false) List<String> movementTypes,
            @ApiParam(value = "Returns only the assessments for the current sentence if true, otherwise all previous sentences are included", defaultValue = "true") @RequestParam(value = "latestOnly", required = false, defaultValue = "true") Boolean latestOnly);

    @GetMapping("/{agencyId}/enroute")
    @ApiOperation(value = "Enroute prisoner movement details.", notes = "Enroute to reception", nickname = "getEnrouteOffenderMovements")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderMovement> getEnrouteOffenderMovements(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId,
            @ApiParam(value = "Optional filter on date of movement") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "movementDate", required = false) LocalDate movementDate);

    @GetMapping("/rollcount/{agencyId}/enroute")
    @ApiOperation(value = "Enroute prisoner movement count.", notes = "Enroute to reception count", nickname = "getEnrouteOffenderMovementCount")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    int getEnrouteOffenderMovementCount(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId, @ApiParam(value = "Optional filter on date of movement. Defaults to today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "movementDate", required = false) LocalDate movementDate);

    @GetMapping("/{agencyId}/in/{isoDate}")
    @ApiOperation(value = "Information on offenders in today.", notes = "Information on offenders in on given date.", nickname = "getMovementsInOnDate")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderIn> getMovementsIn(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId,
            @ApiParam(value = "date", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable("isoDate") LocalDate movementsDate);

    @GetMapping("/{agencyId}/in")
    @ApiOperation(value = "Offenders who entered a prison during a time period.", notes = "Offenders who entered a prison during a time period.", nickname = "getMovementsInBetweeen")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<List<OffenderIn>> getMovementsIn(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId,
            @ApiParam(value = "fromDateTime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDateTime") LocalDateTime fromDate,
            @ApiParam(value = "toDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "toDateTime", required = false) LocalDateTime toDate,
            @ApiParam(value = "Requested offset of first record in returned collection of prisoner records.", defaultValue = "0")
            @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
            @ApiParam(value = "Requested limit to number of records returned.", defaultValue = "10")
            @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit);

    @GetMapping("/livingUnit/{livingUnitId}/currently-out")
    @ApiOperation(value = "Information on offenders currently out.", notes = "Information on offenders currently out.", nickname = "getOffendersCurrentlyOut")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderOut> getOffendersCurrentlyOut(
            @ApiParam(value = "The identifier of a living unit, otherwise known as an internal location.", required = true) @PathVariable("livingUnitId") Long livingUnitId);

    @GetMapping("/agency/{agencyId}/currently-out")
    @ApiOperation(value = "Information on offenders currently out.", notes = "Information on offenders currently out.", nickname = "getOffendersCurrentlyOut")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderOut> getOffendersCurrentlyOut(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId);

    @GetMapping("/{agencyId}/out/{isoDate}")
    @ApiOperation(value = "", nickname = "getOffendersOut")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderOutTodayDto> getOffendersOutToday(
            @ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId,
            @ApiParam(value = "date", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable("isoDate") LocalDate movementsDate);

    @GetMapping("/rollcount/{agencyId}/in-reception")
    @ApiOperation(value = "", nickname = "getOffendersInReception")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderInReception> getOffendersInReception(@ApiParam(value = "The prison id", required = true) @PathVariable("agencyId") String agencyId);

    @GetMapping("/transfers")
    @ApiOperation(value = "Information on scheduled court, transfer and release events, and confirmed movements between two dates/times for a specified number of agencies.",
            notes = "Planned movements are recorded as events of type court, release or transfers/appointments. When these events are started they are actualised as external movements.",
            nickname = "getTransfers")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid agency identifiers, or from time after the to time, or a time period greater than 24 hours specified, or parameter format not correct.", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "The token presented did not contain the necessary role to access this resource.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "The token presented has expired.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    TransferSummary getTransfers(
            @ApiParam(value = "One or more agencyId values eg.agencyId=LEI&agencyId=MDI", required = true) @NotEmpty @RequestParam("agencyId") List<String> agencyIds,
            @ApiParam(value = "From date and time ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDateTime") LocalDateTime fromDateTime,
            @ApiParam(value = "To date and time in ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("toDateTime") LocalDateTime toDateTime,
            @ApiParam(value = "Set to true to include planned court events", required = false, defaultValue = "false") @RequestParam(value = "courtEvents", required = false, defaultValue = "false") boolean courtEvents,
            @ApiParam(value = "Set to true to include planned release events", required = false, defaultValue = "false") @RequestParam(value = "releaseEvents", required = false, defaultValue = "false") boolean releaseEvents,
            @ApiParam(value = "Set to true to include planned transfer/appointment events", required = false, defaultValue = "false") @RequestParam(value = "transferEvents", required = false, defaultValue = "false") boolean transferEvents,
            @ApiParam(value = "Set to true to include confirmed movements", required = false, defaultValue = "false") @RequestParam(value = "movements", required = false, defaultValue = "false") boolean movements);

    @GetMapping("/upcomingCourtAppearances")
    @ApiOperation(value = "Get future court hearings for all offenders", nickname = "getUpcomingCourtAppearances")
    List<CourtEventBasic> getUpcomingCourtAppearances();
}
