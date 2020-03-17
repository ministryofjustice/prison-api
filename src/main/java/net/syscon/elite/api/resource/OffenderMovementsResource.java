package net.syscon.elite.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.api.model.CourtHearings;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = {"/bookings"})
public interface OffenderMovementsResource {

    @PostMapping("/{bookingId}/court-cases/{courtCaseId}/prison-to-court-hearings")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Schedules a prison to court hearing for an offender.", notes = "Schedules a prison to court hearing for an offender.", nickname = "prisonToCourt")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Court hearing created.", response = CourtHearing.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    CourtHearing prisonToCourt(@ApiParam(value = "The offender booking to associate the court hearing with.", required = true) @PathVariable("bookingId") Long bookingId,
                               @ApiParam(value = "The court case to associate the hearing with.", required = true) @PathVariable("courtCaseId") Long courtCaseId,
                               @ApiParam(value = "The prison to court hearing to be scheduled for the offender booking.", required = true) @RequestBody PrisonToCourtHearing hearing);

    @ApiIgnore
    @GetMapping("{bookingId}/court-hearings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CourtHearings.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    CourtHearings getCourtHearings(@ApiParam(value = "The offender booking linked to the court hearings.", required = true) @PathVariable("bookingId") Long bookingId);
}
