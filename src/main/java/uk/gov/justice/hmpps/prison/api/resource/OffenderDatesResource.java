package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.OffenderDatesService;

@RestController
@Validated
@Tag(name = "offender-dates")
@RequestMapping("${api.base.path}/offender-dates")
@AllArgsConstructor
public class OffenderDatesResource {

    private final OffenderDatesService offenderDatesService;

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Offender key dates calculation created", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SentenceCalcDates.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to update an offender's dates", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Update the key dates for an offender.", description = "Requires RELEASE_DATES_CALCULATOR")
    @PostMapping("/{bookingId}")
    @PreAuthorize("hasRole('RELEASE_DATES_CALCULATOR') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ResponseEntity<SentenceCalcDates> updateOffenderKeyDates(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId,
                                                                    @RequestBody final RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(offenderDatesService.updateOffenderKeyDates(bookingId, requestToUpdateOffenderDates));
    }
}
