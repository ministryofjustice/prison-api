package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.service.BookingService;

import java.util.List;

@RestController
@Api(tags = {"offender-relationships"})
@Validated
@RequestMapping("${api.base.path}/offender-relationships")
public class OffenderRelationshipResource {

    private final BookingService bookingService;

    public OffenderRelationshipResource(final BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderSummary.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of offenders", notes = "List of offenders", nickname = "getBookingsByExternalRefAndType")
    @GetMapping("/externalRef/{externalRef}/{relationshipType}")
    public List<OffenderSummary> getBookingsByExternalRefAndType(@PathVariable("externalRef") @ApiParam(value = "External Unique Reference to Contact Person", required = true) final String externalRef, @PathVariable("relationshipType") @ApiParam(value = "Relationship Type", required = true) final String relationshipType) {
        return bookingService.getBookingsByExternalRefAndType(externalRef, relationshipType);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderSummary.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of offenders that are related to this person Id and relationship type", notes = "List of offenders that are related to this person Id and relationship type", nickname = "getBookingsByPersonIdAndType")
    @GetMapping("/person/{personId}/{relationshipType}")
    public List<OffenderSummary> getBookingsByPersonIdAndType(@PathVariable("personId") @ApiParam(value = "Person Id of the contact person", required = true) final Long personId, @PathVariable("relationshipType") @ApiParam(value = "Relationship Type", required = true) final String relationshipType) {
        return bookingService.getBookingsByPersonIdAndType(personId, relationshipType);
    }
}
