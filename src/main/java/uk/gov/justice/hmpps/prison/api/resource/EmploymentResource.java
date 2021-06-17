package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderEmploymentResponse;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = {"employment"})
@RequestMapping("${api.base.path}/employment")
public class EmploymentResource {

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "A list of offender employments.", notes = "A list of offender employments.", nickname = "getPrisonerEmployments")
    @GetMapping("/prisoner/{offenderNo}")
    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
    public ResponseEntity<Page<OffenderEmploymentResponse>> getPrisonerEmployments(
        @PathVariable(value = "offenderNo") @ApiParam(value = "List of offender NOMS numbers. NOMS numbers have the format:<b>ANNNNAA</b>") final String offenderNo,
        @RequestParam(value = "page", defaultValue = "0", required = false) @ApiParam(value = "The page number of the paged results", defaultValue = "0") final Integer page,
        @RequestParam(value = "size", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of results returned.", defaultValue = "10") final Integer size
    ) {
        return null;
    }
}
