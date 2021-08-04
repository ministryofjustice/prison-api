package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalc;
import uk.gov.justice.hmpps.prison.service.OffenderSentenceCalcService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = {"sentence-calc"})
@RequestMapping("${api.base.path}/sentence-calc")
public class SentenceCalcResource {

    private final OffenderSentenceCalcService offenderSentenceCalcService;

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "A list of offender educations.", notes = "A list of offender educations.", nickname = "getPrisonerEducations")
    @GetMapping("/{offenderNo}")
    @PreAuthorize("hasRole('SENTENCE_CALC') and hasAuthority('SCOPE_write')")

    public OffenderSentenceCalc addCalculatedDates(
        @PathVariable(value = "offenderNo") @ApiParam(value = "List of offender NOMS numbers. NOMS numbers have the format:<b>G0364GX</b>") final String offenderNo,
        OffenderSentenceCalc offenderSentenceCalc
    ) {

        return offenderSentenceCalcService.addCalculatedDates(offenderNo, offenderSentenceCalc);
    }
}
