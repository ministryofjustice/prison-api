package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AdjudicationsService;

import javax.validation.Valid;
import java.util.List;

@Hidden
@RestController
@Tag(name = "adjudications")
@Validated
@RequestMapping("${api.base.path}/adjudications")
public class AdjudicationsResource {
    private final AdjudicationsService adjudicationsService;

    public AdjudicationsResource(final AdjudicationsService adjudicationsService) {
        this.adjudicationsService = adjudicationsService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. because no incident statement was provided.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No match was found for the provided booking id.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Record an adjudication.", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PostMapping("/adjudication")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    public ResponseEntity<AdjudicationDetail> createAdjudication(@Valid @RequestBody @Parameter(description = "Adjudication details to save", required = true) final NewAdjudication adjudicationDetails) {
        final var savedAdjudication = adjudicationsService.createAdjudication(adjudicationDetails.getOffenderNo(), adjudicationDetails);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedAdjudication);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. because no incident statement was provided.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No match was found for the provided booking id.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Update a recorded adjudication.", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PutMapping("/adjudication/{adjudicationNumber}")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    public ResponseEntity<AdjudicationDetail> updateAdjudication(
        @PathVariable("adjudicationNumber")
        @Parameter(description = "The adjudication number", required = true) final Long adjudicationNumber,
        @Valid @RequestBody @Parameter(description = "Adjudication details to save", required = true) final UpdateAdjudication adjudicationDetails)
    {
        final var savedAdjudication = adjudicationsService.updateAdjudication(adjudicationNumber, adjudicationDetails);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedAdjudication);
    }

    @Deprecated // This is only used for the first version of the Adjudications project - we will eventually remove
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Get details of an existing adjudication.", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @GetMapping("/adjudication/{adjudicationNumber}")
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS')")
    public AdjudicationDetail getAdjudication(
        @PathVariable("adjudicationNumber")
        @Parameter(description = "The adjudication number", required = true) final Long adjudicationNumber
    ) {
        return adjudicationsService.getAdjudication(adjudicationNumber);
    }

    @Deprecated // This is only used for the first version of the Adjudications project - we will eventually remove
    @Operation(summary = "Gets a list of adjudication details for a list of adjudication numbers", description = "Requires MAINTAIN_ADJUDICATIONS access")
    @PostMapping
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS')")
    public List<AdjudicationDetail> getAdjudications(@Parameter(description = "The adjudication numbers", required = true, example = "[1,2,3]") @RequestBody final List<Long> adjudicationNumbers) {
        return adjudicationsService.getAdjudications(adjudicationNumbers);
    }
}
