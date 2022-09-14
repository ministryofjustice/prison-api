package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.*;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.curfews.OffenderCurfewService;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "offender-fine-payment")
@Validated
@RequestMapping(value = "${api.base.path}/offender-fine-payment", produces = "application/json")
public class OffenderFinePaymentResource {
    private final BookingService bookingService;

    public OffenderFinePaymentResource(
            final BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offender fine payment details for a prisoner.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OffenderFinePaymentDto.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender fine payments for a prisoner")
    @GetMapping("/booking/{bookingId}")
    public List<OffenderFinePaymentDto> getOffenderFinePayments(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId) {
        return bookingService.getOffenderFinePayments(bookingId);
    }
}
