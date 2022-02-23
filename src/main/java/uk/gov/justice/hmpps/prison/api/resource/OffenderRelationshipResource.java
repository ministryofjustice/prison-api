package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "offender-relationships")
@Validated
@RequestMapping("${api.base.path}/offender-relationships")
public class OffenderRelationshipResource {

    private final BookingService bookingService;

    public OffenderRelationshipResource(final BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders", description = "List of offenders")
    @GetMapping("/externalRef/{externalRef}/{relationshipType}")
    public List<OffenderSummary> getBookingsByExternalRefAndType(@PathVariable("externalRef") @Parameter(description = "External Unique Reference to Contact Person", required = true) final String externalRef, @PathVariable("relationshipType") @Parameter(description = "Relationship Type", required = true) final String relationshipType) {
        return bookingService.getBookingsByExternalRefAndType(externalRef, relationshipType);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders that are related to this person Id and relationship type", description = "List of offenders that are related to this person Id and relationship type")
    @GetMapping("/person/{personId}/{relationshipType}")
    public List<OffenderSummary> getBookingsByPersonIdAndType(@PathVariable("personId") @Parameter(description = "Person Id of the contact person", required = true) final Long personId, @PathVariable("relationshipType") @Parameter(description = "Relationship Type", required = true) final String relationshipType) {
        return bookingService.getBookingsByPersonIdAndType(personId, relationshipType);
    }
}
