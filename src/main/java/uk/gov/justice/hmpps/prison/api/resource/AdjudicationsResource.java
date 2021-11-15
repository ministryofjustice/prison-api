package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationSearchRequest;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AdjudicationsService;

import javax.validation.Valid;
import java.util.List;

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
    @ApiOperation(value = "Record an adjudication.", notes = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
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
    @ApiOperation(value = "Get details of an existing adjudication.", notes = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @GetMapping("/adjudication/{adjudicationNumber}")
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS')")
    public AdjudicationDetail getAdjudication(
        @PathVariable("adjudicationNumber")
        @ApiParam(value = "The adjudication number", required = true) final Long adjudicationNumber
    ) {
        return adjudicationsService.getAdjudication(adjudicationNumber);
    }

    @ApiOperation(value = "Gets a list of adjudication details for a list of adjudication numbers", notes = "Requires MAINTAIN_ADJUDICATIONS access")
    @PostMapping
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS')")
    public List<AdjudicationDetail> getAdjudications(@ApiParam(value = "The adjudication numbers", required = true, example = "[1,2,3]") @RequestBody final List<Long> adjudicationNumbers) {
        return adjudicationsService.getAdjudications(adjudicationNumbers);
    }

    @ApiOperation(value = "Search of adjudications", notes = "Requires MAINTAIN_ADJUDICATIONS access")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "java.lang.Integer", paramType = "query",
            value = "Results page you want to retrieve (0..N). Default 0, e.g. the first page", example = "0"),
        @ApiImplicitParam(name = "size", dataType = "java.lang.Integer", paramType = "query",
            value = "Number of records per page. Default 20"),
        @ApiImplicitParam(name = "sort", dataType = "java.lang.String", paramType = "query",
            value = "Sort as combined comma separated property and uppercase direction. Multiple sort params allowed to sort by multiple properties. Default to incidentDate,incidentTime ASC")})
    @PostMapping("/search")
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS')")
    public Page<AdjudicationDetail> search(@ApiParam(value = "The adjudication search request", required = true) @RequestBody final AdjudicationSearchRequest searchRequest,
                                           @ApiIgnore
                                           @PageableDefault(sort = {"incidentDate", "incidentTime"}, direction = Sort.Direction.DESC, size = 20) final Pageable pageable) {
        return adjudicationsService.search(searchRequest, pageable);
    }
}
