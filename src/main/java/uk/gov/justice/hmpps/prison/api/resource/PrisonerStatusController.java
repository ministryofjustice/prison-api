package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInformation;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.PrisonerInformationService;

@Slf4j
@RestController
@Validated
@Tag(name = "prisoners")
@RequestMapping(value = "${api.base.path}/prisoners", produces = "application/json")
@AllArgsConstructor
public class PrisonerStatusController {

    private final PrisonerInformationService service;

    @GetMapping("/{offenderNo}/full-status")
    @Operation(summary = "Status and core offender information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PrisonerInformation.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "User not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public PrisonerInformation getPrisonerInformationById(@Parameter(name = "offenderNo", description = "Offender No (NOMS ID)", required = true, example = "A1234AA")
                                                         @PathVariable("offenderNo") final String offenderNo) {
        return service.getPrisonerInformationById(offenderNo);
    }
}
