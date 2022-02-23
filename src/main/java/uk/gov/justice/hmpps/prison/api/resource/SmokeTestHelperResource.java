package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.SmokeTestHelperService;

import javax.validation.constraints.NotNull;

@RestController
@Tag(name = "smoketest")
@RequestMapping("${api.base.path}/smoketest")
@Validated
@Slf4j
@ConditionalOnProperty(name = "smoke.test.aware", havingValue = "true")
public class SmokeTestHelperResource {
    private final SmokeTestHelperService service;

    public SmokeTestHelperResource(SmokeTestHelperService service) {
        this.service = service;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_SMOKE_TEST", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Sets imprisonment status smoke test data for this offender")
    @PostMapping("/offenders/{offenderNo}/imprisonment-status")
    @ProxyUser
    public void imprisonmentDataSetup(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo) {
        service.imprisonmentDataSetup(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "403", description = "Requires role ROLE_SMOKE_TEST", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Releases this offender, with smoke test data")
    @PutMapping("/offenders/{offenderNo}/release")
    @ProxyUser
    public void releasePrisoner(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo) {
        service.releasePrisoner(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "403", description = "Requires role ROLE_SMOKE_TEST", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Recalls this offender, with smoke test data")
    @PutMapping("/offenders/{offenderNo}/recall")
    @ProxyUser
    public void recallPrisoner(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo) {
        service.recallPrisoner(offenderNo);
    }
}
