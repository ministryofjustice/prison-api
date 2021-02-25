package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.LocationService;
import uk.gov.justice.hmpps.prison.service.SearchOffenderService;
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

@RestController
@Api(tags = {"locations"})
@Validated
@RequestMapping("${api.base.path}/locations")
public class LocationResource {
    private final AuthenticationFacade authenticationFacade;
    private final LocationService locationService;
    private final SearchOffenderService searchOffenderService;

    public LocationResource(final AuthenticationFacade authenticationFacade, final LocationService locationService, final SearchOffenderService searchOffenderService) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.searchOffenderService = searchOffenderService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of offenders at location.", notes = "List of offenders at location.", nickname = "getOffendersAtLocationDescription")
    @GetMapping("/description/{locationPrefix}/inmates")
    public ResponseEntity<List<OffenderBooking>> getOffendersAtLocationDescription(
            @PathVariable("locationPrefix") @ApiParam(value = "", required = true) final String locationPrefix,
            @RequestParam(value = "keywords", required = false) @ApiParam("offender name or id to match") final String keywords,
            @RequestParam(value = "fromDob", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Offenders with a DOB >= this date", example = "1970-01-02") final LocalDate fromDob,
            @RequestParam(value = "toDob", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Offenders with a DOB <= this date", example = "1975-01-02") final LocalDate toDob,
            @RequestParam(value = "alerts", required = false) @ApiParam("alert flags to filter by") final List<String> alerts,
            @RequestParam(value = "returnIep", required = false, defaultValue = "false") @ApiParam(value = "return IEP data", defaultValue = "false") final boolean returnIep,
            @RequestParam(value = "returnAlerts", required = false, defaultValue = "false") @ApiParam(value = "return Alert data", defaultValue = "false") final boolean returnAlerts,
            @RequestParam(value = "returnCategory", defaultValue = "false") @ApiParam(value = "retrieve category classification from assessments", defaultValue = "false") final boolean returnCategory,
            @RequestParam(value = "convictedStatus", defaultValue = "All") @ApiParam(value = "retrieve inmates with a specific convicted status (Convicted, Remand, default: All)", defaultValue = "All") final String convictedStatus,
            @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of inmate records.", defaultValue = "0") final Long pageOffset,
            @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of inmate records returned.", defaultValue = "10") final Long pageLimit,
            @RequestHeader(value = "Sort-Fields", defaultValue = "lastName,firstName,bookingId", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") final String sortFields,
            @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .keywords(keywords)
                .locationPrefix(locationPrefix)
                .returnAlerts(returnAlerts)
                .returnIep(returnIep)
                .returnCategory(returnCategory)
                .convictedStatus(convictedStatus)
                .alerts(alerts)
                .fromDob(fromDob)
                .toDob(toDob)
                .orderBy(sortFields)
                .order(sortOrder)
                .offset(nvl(pageOffset, 0L))
                .limit(nvl(pageLimit, 10L))
                .build();

        final var offenders = searchOffenderService.findOffenders(request);

        return ResponseEntity.ok()
                .headers(offenders.getPaginationHeaders())
                .body(offenders.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Location.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Location detail.", notes = "Location detail.", nickname = "getLocation")
    @GetMapping("/{locationId}")
    public Location getLocation(
        @PathVariable("locationId")
        @ApiParam(value = "The location id of location", required = true)
        final Long locationId,

        @RequestParam(value="includeInactive", required = false)
        @Pattern(regexp="Yes", flags = {Flag.CASE_INSENSITIVE})
        @ApiParam(value = "Match a location that is inactive?", allowableValues = "Yes")
        final String includeInactive
    ) {
        return locationService.getLocation(locationId, "Yes".equalsIgnoreCase(includeInactive));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of offenders at location.", notes = "List of offenders at location.", nickname = "getOffendersAtLocation")
    @GetMapping("/{locationId}/inmates")
    public ResponseEntity<List<OffenderBooking>> getOffendersAtLocation(@PathVariable("locationId") @ApiParam(value = "The location id of location", required = true) final Long locationId, @RequestParam(value = "query", required = false) @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</p> ", required = true) final String query, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of inmate records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of inmate records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var inmates = locationService.getInmatesFromLocation(
                locationId,
                authenticationFacade.getCurrentUsername(),
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(inmates.getPaginationHeaders())
                .body(inmates.getItems());
    }
}
