package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
        @ApiResponse(code = 400, message = "Invalid request - e.g. because no incident statement was provided.", response = ErrorResponse.class)
    })
    @ApiOperation(value = "Record an adjudication.", notes = "Requires SYSTEM access")
    @PostMapping("/adjudication")
    @ProxyUser
    public ResponseEntity<AdjudicationDetail> createAdjudication(@Valid @RequestBody @ApiParam(value = "Adjudication details to save", required = true) final NewAdjudication adjudicationDetails) {
        final var savedAdjudication = adjudicationsService.createAdjudication(adjudicationDetails.getBookingId(), adjudicationDetails);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedAdjudication);
    }
}
