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
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Offence;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Sentence;
import uk.gov.justice.hmpps.prison.service.BookingService;
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
    public ResponseEntity<Long> createCourtCase(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                               @RequestBody final CourtCase courtCase) {
        return ResponseEntity.status(HttpStatus.CREATED).body(digitalWarrantService.createCourtCase(bookingId, courtCase));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Court case offence created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create a Court case offence")
    @PostMapping("/booking/{bookingId}/offence")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Long> createOffence(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                                @RequestBody final Offence offence) {
        return ResponseEntity.status(HttpStatus.CREATED).body(digitalWarrantService.createOffenderOffence(bookingId, offence));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sentence created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create a sentence")
    @PostMapping("/booking/{bookingId}/sentence")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Integer> createSentence(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId,
                                                @RequestBody final Sentence sentence) {
        return ResponseEntity.status(HttpStatus.CREATED).body(digitalWarrantService.createOffenderSentence(bookingId, sentence));
    }
}
