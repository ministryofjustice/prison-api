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
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMark;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.PersonService;
import java.util.List;

@RestController
@Tag(name = "identifying-marks")
@Validated
@RequestMapping(value = "${api.base.path}/identifying-marks", produces = "application/json")
public class IdentifyingMarksResource {
    private final PersonService service;

    public IdentifyingMarksResource(final PersonService service) {
        this.service = service;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "The most recent value of each type of person identifier", description = "Requires role ROLE_VIEW_CONTACTS")
    @VerifyOffenderAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    @GetMapping("/prisoner/{offenderNo}")
    public List<IdentifyingMark> getIdentifyingMarksForLatestBooking(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", required = true) final String offenderNo) {
        return List.of();
    }
}
