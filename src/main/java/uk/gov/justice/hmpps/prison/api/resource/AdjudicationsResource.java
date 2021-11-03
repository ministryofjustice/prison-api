package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
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
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AdjudicationsService;

import javax.validation.Valid;

@Hidden
@RestController
@Api(tags = {"adjudications"})
@Validated
@RequestMapping("${api.base.path}/adjudications")
public class AdjudicationsResource {
    private final AdjudicationsService adjudicationsService;

    public AdjudicationsResource(final AdjudicationsService adjudicationsService) {
        this.adjudicationsService = adjudicationsService;
    }

    @ApiResponses({
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 400, message = "Invalid request - e.g. because no incident statement was provided.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "No match was found for the provided booking id.", response = ErrorResponse.class)
    })
    @ApiOperation(value = "Record an adjudication.", notes = "Requires SYSTEM access")
    @PostMapping("/adjudication")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    public ResponseEntity<AdjudicationDetail> createAdjudication(@Valid @RequestBody @ApiParam(value = "Adjudication details to save", required = true) final NewAdjudication adjudicationDetails) {
        final var savedAdjudication = adjudicationsService.createAdjudication(adjudicationDetails.getOffenderNo(), adjudicationDetails);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedAdjudication);
    }

    @ApiResponses({
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    @ApiOperation(value = "Get details of an existing adjudication.", notes = "Requires SYSTEM access")
    @GetMapping("/adjudication/{adjudicationNumber}")
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS')")
    public AdjudicationDetail getAdjudication(
        @PathVariable("adjudicationNumber")
        @ApiParam(value = "The adjudication number", required = true)
        final Long adjudicationNumber
    ) {
        return adjudicationsService.getAdjudication(adjudicationNumber);
    }
}
