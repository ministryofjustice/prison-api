package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;

import java.util.List;

@Api(tags = {"/offender-relationships"})
@SuppressWarnings("unused")
public interface OffenderRelationshipResource {

    @GetMapping("/externalRef/{externalRef}/{relationshipType}")


    @ApiOperation(value = "List of offenders", notes = "List of offenders", nickname = "getBookingsByExternalRefAndType")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderSummary.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderSummary> getBookingsByExternalRefAndType(@ApiParam(value = "External Unique Reference to Contact Person", required = true) @PathVariable("externalRef") String externalRef,
                                                                            @ApiParam(value = "Relationship Type", required = true) @PathVariable("relationshipType") String relationshipType);

    @GetMapping("/person/{personId}/{relationshipType}")


    @ApiOperation(value = "List of offenders that are related to this person Id and relationship type", notes = "List of offenders that are related to this person Id and relationship type", nickname = "getBookingsByPersonIdAndType")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderSummary.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderSummary> getBookingsByPersonIdAndType(@ApiParam(value = "Person Id of the contact person", required = true) @PathVariable("personId") Long personId,
                                                                      @ApiParam(value = "Relationship Type", required = true) @PathVariable("relationshipType") String relationshipType);


}
