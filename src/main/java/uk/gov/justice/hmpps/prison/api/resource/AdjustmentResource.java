package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = {"adjustments"})
@RequestMapping("${api.base.path}/adjustments")
@AllArgsConstructor
public class AdjustmentResource {

    private final AdjustmentService adjustmentService;

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender sentence adjustments.", nickname = "getBookingSentenceAdjustments")
    @GetMapping("/{bookingId}/sentence-and-booking")
    public BookingAndSentenceAdjustments getBookingAndSentenceAdjustments(@PathVariable("bookingId") @ApiParam(value = "The booking id of the offender", required = true) final Long bookingId) {
        return adjustmentService.getBookingAndSentenceAdjustments(bookingId);
    }
}
