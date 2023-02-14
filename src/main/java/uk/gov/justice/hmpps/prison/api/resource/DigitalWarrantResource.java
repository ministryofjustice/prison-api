package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Adjustment;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantCourtCase;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantCharge;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.CourtDateResult;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantSentence;
import uk.gov.justice.hmpps.prison.service.digitalwarrant.DigitalWarrantService;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "digital-warrant")
@Validated
@RequestMapping(value = "${api.base.path}/digital-warrant", produces = "application/json")
public class DigitalWarrantResource {
    @Autowired
    private DigitalWarrantService digitalWarrantService;

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Court case created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create a court case")
    @PostMapping("/booking/{bookingId}/court-case")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createCourtCase(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                               @RequestBody final WarrantCourtCase courtCase) {
        return digitalWarrantService.createCourtCase(bookingId, courtCase);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Court case charge created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create a Court case charge")
    @PostMapping("/booking/{bookingId}/charge")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createCharge(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                                @RequestBody final WarrantCharge charge) {
        return digitalWarrantService.createCharge(bookingId, charge);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sentence created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create a sentence")
    @PostMapping("/booking/{bookingId}/sentence")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public Integer createSentence(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                                @RequestBody final WarrantSentence sentence) {
        return digitalWarrantService.createOffenderSentence(bookingId, sentence);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sentence created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create a sentence")
    @PostMapping("/booking/{bookingId}/adjustment")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createAdjustment(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                  @RequestBody final Adjustment adjustment) {
        return digitalWarrantService.createAdjustment(bookingId, adjustment);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The court date results.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtDateResult.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns details of all court dates and the result of each.")
    @GetMapping("/court-date-results/{offenderId}")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_read')")
    public List<CourtDateResult> getCourtDateResults(@PathVariable("offenderId") @Parameter(description = "The required offender id (mandatory)", required = true) final String offenderId) {
        return digitalWarrantService.getCourtDateResults(offenderId);
    }
}

