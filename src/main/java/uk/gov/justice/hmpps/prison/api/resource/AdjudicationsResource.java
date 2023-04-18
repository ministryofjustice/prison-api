package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationCreationRequestData;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.OicHearingRequest;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResponse;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultDto;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultRequest;
import uk.gov.justice.hmpps.prison.api.model.OicSanctionRequest;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Sanction;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AdjudicationsService;

import jakarta.validation.Valid;
import java.util.List;

@Hidden
@RestController
@Tag(name = "adjudications")
@Validated
@RequestMapping(value = "${api.base.path}/adjudications", produces = "application/json")
public class AdjudicationsResource {
    private final AdjudicationsService adjudicationsService;

    public AdjudicationsResource(final AdjudicationsService adjudicationsService) {
        this.adjudicationsService = adjudicationsService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. no current booking was found for the given offender.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No match was found for the provided offender number.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Request the information needed to create an adjudication.",
        description = "Must be called before creating the adjudication. Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PostMapping("/adjudication/request-creation-data")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    public ResponseEntity<AdjudicationCreationRequestData> requestAdjudicationCreationData(@RequestBody final String offenderNo) {
        final var adjudicationCreationRequest = adjudicationsService.generateAdjudicationCreationData(offenderNo);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(adjudicationCreationRequest);
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

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "400", description = "Invalid request - ie missing hearing location or date", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Creates an OIC hearing", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PostMapping("/adjudication/{adjudicationNumber}/hearing")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public OicHearingResponse createOicHearing(
        @Valid @RequestBody @Parameter(description = "OIC hearing to save", required = true) final OicHearingRequest oicHearingRequest,
        @PathVariable("adjudicationNumber") final Long adjudicationNumber
        ) {
        return adjudicationsService.createOicHearing(adjudicationNumber, oicHearingRequest);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Oic Hearing updated"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "400", description = "Invalid request - ie missing hearing location or date, or the hearing does not belong to the adjudication", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Amends an OIC hearing", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PutMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.OK)
    public void amendOicHearing(
        @Valid @RequestBody @Parameter(description = "OIC hearing to amend", required = true) final OicHearingRequest oicHearingRequest,
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId
    ) {
        adjudicationsService.amendOicHearing(adjudicationNumber, oicHearingId, oicHearingRequest);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hearing was deleted"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "400", description = "Invalid request - the hearing does not belong to the adjudication", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing id", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "deletes an OIC hearing", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @DeleteMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    public void deleteOicHearing(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId
    ) {
        adjudicationsService.deleteOicHearing(adjudicationNumber, oicHearingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Creates an OIC hearing result", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PostMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}/result")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public OicHearingResultDto createOicHearingResult(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId,
        @Valid @RequestBody @Parameter(description = "OIC hearing result to save", required = true) final OicHearingResultRequest oicHearingResultRequest
    ) {
        return adjudicationsService.createOicHearingResult(adjudicationNumber, oicHearingId, oicHearingResultRequest);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Updates an OIC hearing result", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PutMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}/result")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.OK)
    public OicHearingResultDto amendOicHearingResult(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId,
        @Valid @RequestBody @Parameter(description = "Amended OIC hearing result to save", required = true) final OicHearingResultRequest oicHearingResultRequest
    ) {
        return adjudicationsService.amendOicHearingResult(adjudicationNumber, oicHearingId, oicHearingResultRequest);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Deletes an OIC hearing result", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @DeleteMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}/result")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteOicHearingResult(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId
    ) {
        adjudicationsService.deleteOicHearingResult(adjudicationNumber, oicHearingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Creates an OIC sanction", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PostMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}/sanction")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.CREATED)
    public Sanction createOicSanction(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId,
        @Valid @RequestBody @Parameter(description = "OIC sanctions to save", required = true) final List<OicSanctionRequest> oicSanctionRequests
    ) {
        return adjudicationsService.createOicSanction(adjudicationNumber, oicHearingId, oicSanctionRequests);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Updates an OIC hearing result", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @PutMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}/sanction")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.OK)
    public OicHearingResultDto amendOicSanction(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId,
        @Valid @RequestBody @Parameter(description = "Amended OIC sanction to save", required = true) final OicHearingResultRequest oicHearingResultRequest
    ) {
        return null;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "403", description = "The client is not authorised for this operation"),
        @ApiResponse(responseCode = "404", description = "No match was found for the adjudication number or hearing", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Deletes an OIC sanction", description = "Requires MAINTAIN_ADJUDICATIONS access and write scope")
    @DeleteMapping("/adjudication/{adjudicationNumber}/hearing/{oicHearingId}/sanction")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_ADJUDICATIONS') and hasAuthority('SCOPE_write')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteOicSanction(
        @PathVariable("adjudicationNumber") final Long adjudicationNumber,
        @PathVariable("oicHearingId") final Long oicHearingId
    ) {

    }
}
