package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.justice.hmpps.prison.core.ReferenceData;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.LocationService;
import uk.gov.justice.hmpps.prison.service.SearchOffenderService;
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

@RestController
@Tag(name = "locations")
@Validated
@RequestMapping(value = "${api.base.path}/locations", produces = "application/json")
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
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders at location.", description = "List of offenders at location.")
    @GetMapping("/description/{locationPrefix}/inmates")
    @SlowReportQuery
    public ResponseEntity<List<OffenderBooking>> getOffendersAtLocationDescription(
            @PathVariable("locationPrefix") @Parameter(required = true) final String locationPrefix,
            @RequestParam(value = "keywords", required = false) @Parameter(description = "offender name or id to match") final String keywords,
            @RequestParam(value = "fromDob", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Offenders with a DOB >= this date", example = "1970-01-02") final LocalDate fromDob,
            @RequestParam(value = "toDob", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Offenders with a DOB <= this date", example = "1975-01-02") final LocalDate toDob,
            @RequestParam(value = "alerts", required = false) @Parameter(description = "alert flags to filter by") final List<String> alerts,
            @RequestParam(value = "returnAlerts", required = false, defaultValue = "false") @Parameter(description = "return Alert data") final boolean returnAlerts,
            @RequestParam(value = "returnCategory", defaultValue = "false") @Parameter(description = "retrieve category classification from assessments") final boolean returnCategory,
            @RequestParam(value = "convictedStatus", defaultValue = "All") @Parameter(description = "retrieve inmates with a specific convicted status (Convicted, Remand, default: All)") final String convictedStatus,
            @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of inmate records.") final Long pageOffset,
            @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of inmate records returned.") final Long pageLimit,
            @RequestHeader(value = "Sort-Fields", defaultValue = "lastName,firstName,bookingId", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") final String sortFields,
            @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .keywords(keywords)
                .locationPrefix(locationPrefix)
                .returnAlerts(returnAlerts)
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
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Location.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Location detail.", description = "Location detail.")
    @GetMapping("/{locationId}")
    @ReferenceData(description = "Location properties only are returned, no prisoner data")
    public Location getLocation(
        @PathVariable("locationId")
        @Parameter(description = "The location id of location", required = true)
        final Long locationId,

        @RequestParam(value="includeInactive", required = false)
        @Parameter(description = "Match a location that is inactive?", schema = @Schema(implementation = String.class, allowableValues = {"true","false"}))
        final Boolean includeInactive
    ) {
        return locationService.getLocation(locationId, includeInactive != null && includeInactive);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns the location (internal) for a prison based on description")
    @GetMapping("/code/{code}")
    @ReferenceData(description = "Location properties only are returned, no prisoner data")
    public Location getLocationByCode(
        @PathVariable("code") @Parameter(example = "MDI-1", required = true) final String code) {
           return locationService.getLocationByCode(code).orElseThrow(EntityNotFoundException.withId(code));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders at location.", description = "List of offenders at location.")
    @GetMapping("/{locationId}/inmates")
    @SlowReportQuery
    public ResponseEntity<List<OffenderBooking>> getOffendersAtLocation(@PathVariable("locationId") @Parameter(description = "The location id of location", required = true) final Long locationId,
                                                                        @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of inmate records.") final Long pageOffset,
                                                                        @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of inmate records returned.") final Long pageLimit,
                                                                        @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") final String sortFields,
                                                                        @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var inmates = locationService.getInmatesFromLocation(
                locationId,
                authenticationFacade.getCurrentUsername(),
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(inmates.getPaginationHeaders())
                .body(inmates.getItems());
    }
}
