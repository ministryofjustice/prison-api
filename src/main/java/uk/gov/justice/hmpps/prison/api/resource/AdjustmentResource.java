package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.BookingAndSentenceAdjustments;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.service.AdjustmentService;

@RestController
@Validated
@Tag(name = "adjustments")
@RequestMapping("${api.base.path}/adjustments")
@AllArgsConstructor
public class AdjustmentResource {

    private final AdjustmentService adjustmentService;

    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender sentence adjustments.")
    @GetMapping("/{bookingId}/sentence-and-booking")
    public BookingAndSentenceAdjustments getBookingAndSentenceAdjustments(@PathVariable("bookingId") @Parameter(description = "The booking id of the offender", required = true) final Long bookingId) {
        return adjustmentService.getBookingAndSentenceAdjustments(bookingId);
    }
}
