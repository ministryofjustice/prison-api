package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Api(tags = {"/search-offenders"})
@SuppressWarnings("unused")
public interface SearchOffenderResource {

    @GetMapping("/{locationPrefix}/{keywords}")
    @Deprecated
    @ApiOperation(value = "List offenders by location (matching keywords).", notes = "Deprecated: Use <b>/locations/description/{locationPrefix}/inmates</b> instead. This API will be removed in a future release.", nickname = "searchForOffendersLocationAndKeyword")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<OffenderBooking>> searchForOffendersLocationAndKeyword(@ApiParam(value = "", required = true) @PathVariable("locationPrefix") String locationPrefix,
                                                                               @ApiParam(value = "", required = true) @PathVariable("keywords") String keywords,
                                                                               @ApiParam(value = "return IEP data", defaultValue = "false") @RequestParam("returnIep") boolean returnIep,
                                                                               @ApiParam(value = "return Alert data", defaultValue = "false") @RequestParam("returnAlerts") boolean returnAlerts,
                                                                               @ApiParam(value = "Requested offset of first record in returned collection of search-offender records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                               @ApiParam(value = "Requested limit to number of search-offender records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                               @ApiParam(value = "Comma separated list of one or more of the following fields - <b><<fieldsList>></b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                               @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);


}
