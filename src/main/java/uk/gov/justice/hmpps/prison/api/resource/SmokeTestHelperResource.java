package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = {"smoketest"})
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
            @ApiResponse(code = 403, message = "Requires role ROLE_SMOKE_TEST", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class)})
    @ApiOperation(value = "Sets imprisonment status smoke test data for this offender")
    @PostMapping("/offenders/{offenderNo}/imprisonment-status")
    @ProxyUser
    public void imprisonmentDataSetup(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo) {
        service.imprisonmentDataSetup(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(code = 403, message = "Requires role ROLE_SMOKE_TEST", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class)})
    @ApiOperation(value = "Releases this offender, with smoke test data")
    @PutMapping("/offenders/{offenderNo}/release")
    @ProxyUser
    public void releasePrisoner(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo) {
        service.releasePrisoner(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(code = 403, message = "Requires role ROLE_SMOKE_TEST", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class)})
    @ApiOperation(value = "Recalls this offender, with smoke test data")
    @PutMapping("/offenders/{offenderNo}/recall")
    @ProxyUser
    public void recallPrisoner(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo) {
        service.recallPrisoner(offenderNo);
    }
}
