package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.LatestTusedData;
import uk.gov.justice.hmpps.prison.api.model.OffenderCalculatedKeyDates;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalculationSummary;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.OffenderDatesService;

import java.util.List;

@RestController
@Validated
@Tag(name = "offender-dates")
@RequestMapping(value = "${api.base.path}/offender-dates", produces = "application/json")
@AllArgsConstructor
public class OffenderDatesResource {

    private final OffenderDatesService offenderDatesService;
    private final BookingService bookingService;

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

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offender key dates found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OffenderCalculatedKeyDates.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to update an offender's dates", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Get the key dates for an offender.", description = "Requires RELEASE_DATES_CALCULATOR")
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('RELEASE_DATES_CALCULATOR')")
    @ProxyUser
    public ResponseEntity<OffenderCalculatedKeyDates> getOffenderKeyDates(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(offenderDatesService.getOffenderKeyDates(bookingId));
    }


    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offender key dates found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OffenderCalculatedKeyDates.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to get an offender's calculated dates", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Get the key dates for an offender.", description = "Requires RELEASE_DATES_CALCULATOR")
    @GetMapping("/sentence-calculation/{offenderSentCalcId}")
    @PreAuthorize("hasRole('RELEASE_DATES_CALCULATOR')")
    @ProxyUser
    public ResponseEntity<OffenderCalculatedKeyDates> getOffenderKeyDatesForOffenderSentCalcId(@PathVariable("offenderSentCalcId") @Parameter(description = "The Offender Sent Calc Id id of offender", required = true) final Long offenderSentCalcId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(offenderDatesService.getOffenderKeyDatesByOffenderSentCalcId(offenderSentCalcId));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offender calculations found", content = {@Content(mediaType = "application/json", array = @ArraySchema( schema = @Schema(implementation = SentenceCalculationSummary.class)))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to update an offender's dates", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Get the key dates for an offender.", description = "Requires RELEASE_DATES_CALCULATOR")
    @GetMapping("/calculations/{nomsId}")
    @PreAuthorize("hasRole('RELEASE_DATES_CALCULATOR')")
    @ProxyUser
    public ResponseEntity<List<SentenceCalculationSummary>> getOffenderCalculations(@PathVariable("nomsId") @Parameter(description = "The booking id of offender", required = true) final String nomsId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(bookingService.getOffenderSentenceCalculationsForPrisoner(nomsId));
    }
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "TUSED data found for offender", content = {@Content(mediaType = "application/json",  schema = @Schema(implementation = LatestTusedData.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to access latest TUSED information for offender", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Get the latest TUSED data for an offender. If the offender has had a previous TUSED recorded on a previous booking this will return the latest one", description = "Requires RELEASE_DATES_CALCULATOR")
    @GetMapping("/latest-tused/{nomsId}")
    @PreAuthorize("hasRole('RELEASE_DATES_CALCULATOR')")
    @ProxyUser
    public LatestTusedData getOffenderLatestTused(@PathVariable("nomsId") @Parameter(description = "The nomis id of the offender", required = true) final String nomsId) {
        return offenderDatesService.getLatestTusedDataFromNomsId(nomsId);
    }
}
