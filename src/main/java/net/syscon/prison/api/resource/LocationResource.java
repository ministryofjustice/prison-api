package net.syscon.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.Location;
import net.syscon.prison.api.model.OffenderBooking;
import net.syscon.prison.api.support.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/locations"}, description = "Offender Location information")
public interface LocationResource {

    @GetMapping("/description/{locationPrefix}/inmates")
    @ApiOperation(value = "List of offenders at location.", notes = "List of offenders at location.", nickname = "getOffendersAtLocationDescription")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<OffenderBooking>> getOffendersAtLocationDescription(@ApiParam(value = "", required = true) @PathVariable("locationPrefix") String locationPrefix,
                                                                            @ApiParam(value = "offender name or id to match") @RequestParam(value = "keywords", required = false) String keywords,
                                                                            @ApiParam(value = "Offenders with a DOB >= this date", example = "1970-01-02") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDob", required = false) LocalDate fromDob,
                                                                            @ApiParam(value = "Offenders with a DOB <= this date", example = "1975-01-02") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDob", required = false) LocalDate toDob,
                                                                            @ApiParam(value = "alert flags to filter by") @RequestParam(value = "alerts", required = false) List<String> alerts,
                                                                            @ApiParam(value = "return IEP data", defaultValue = "false") @RequestParam(value = "returnIep", required = false, defaultValue = "false") boolean returnIep,
                                                                            @ApiParam(value = "return Alert data", defaultValue = "false") @RequestParam(value = "returnAlerts", required = false, defaultValue = "false") boolean returnAlerts,
                                                                            @ApiParam(value = "retrieve category classification from assessments", defaultValue = "false") @RequestParam(value = "returnCategory", defaultValue = "false") boolean returnCategory,
                                                                            @ApiParam(value = "retrieve inmates with a specific convicted status (Convicted, Remand, default: All)", defaultValue = "All") @RequestParam(value = "convictedStatus", defaultValue = "All") String convictedStatus,
                                                                            @ApiParam(value = "Requested offset of first record in returned collection of inmate records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                            @ApiParam(value = "Requested limit to number of inmate records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                            @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") @RequestHeader(value = "Sort-Fields", defaultValue = "lastName,firstName,bookingId", required = false) String sortFields,
                                                                            @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{locationId}")
    @ApiOperation(value = "Location detail.", notes = "Location detail.", nickname = "getLocation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Location getLocation(@ApiParam(value = "The location id of location", required = true) @PathVariable("locationId") Long locationId);

    @GetMapping("/{locationId}/inmates")
    @ApiOperation(value = "List of offenders at location.", notes = "List of offenders at location.", nickname = "getOffendersAtLocation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<OffenderBooking>> getOffendersAtLocation(@ApiParam(value = "The location id of location", required = true) @PathVariable("locationId") Long locationId,
                                                                 @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</p> ", required = true) @RequestParam(value = "query", required = false) String query,
                                                                 @ApiParam(value = "Requested offset of first record in returned collection of inmate records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                 @ApiParam(value = "Requested limit to number of inmate records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                 @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                 @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

}
