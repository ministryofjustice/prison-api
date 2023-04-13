package uk.gov.justice.hmpps.prison.api.resource;


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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCodeInfo;
import uk.gov.justice.hmpps.prison.api.model.ReferenceDomain;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;


@RestController
@Tag(name = "reference-domains")
@RequestMapping(value = "${api.base.path}/reference-domains", produces = "application/json")
@Validated
public class ReferenceDomainResource {
    private final ReferenceDomainService referenceDomainService;
    private final CaseNoteService caseNoteService;

    public ReferenceDomainResource(final ReferenceDomainService referenceDomainService, final CaseNoteService caseNoteService) {
        this.referenceDomainService = referenceDomainService;
        this.caseNoteService = caseNoteService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of alert types (with alert codes).", description = "List of alert types (with alert codes).")
    @GetMapping("/alertTypes")
    @SlowReportQuery
    public ResponseEntity<List<ReferenceCode>> getAlertTypes(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of alertType records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of alertType records returned.") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>code, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var referenceCodes =
                referenceDomainService.getAlertTypes(
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(referenceCodes.getPaginationHeaders())
                .body(referenceCodes.getItems());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of case note source codes.", description = "List of case note source codes.", hidden = true)
    @GetMapping("/caseNoteSources")
    @SlowReportQuery
    public ResponseEntity<List<ReferenceCode>> getCaseNoteSources(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of caseNoteSource records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of caseNoteSource records returned.") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>code, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var caseNoteSources =
                referenceDomainService.getCaseNoteSources(
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok().headers(caseNoteSources.getPaginationHeaders()).body(caseNoteSources.getItems());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of all used case note types (with sub-types).", description = "List of all used case note types (with sub-types).", hidden = true)
    @GetMapping("/caseNoteTypes")
    @SlowReportQuery
    public  List<ReferenceCode> getCaseNoteTypes() {
        return caseNoteService.getUsedCaseNoteTypesWithSubTypes();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of reference codes for reference domain paged.", description = "List of reference codes for reference domain paged. Please note this API has the incorrect name so the non-paged /domains/{domain}/codes version is preferred.")
    @GetMapping("/domains/{domain}")
    @SlowReportQuery
    public ResponseEntity<List<ReferenceCode>> getReferenceCodesByDomain(@PathVariable("domain") @Parameter(description = "The domain identifier/name.", required = true) final String domain, @RequestParam(value = "withSubCodes", required = false, defaultValue = "false") @Parameter(description = "Specify whether or not to return reference codes with their associated sub-codes.") final boolean withSubCodes, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of domain records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of domain records returned.") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>code, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var referenceCodes =
                referenceDomainService.getReferenceCodesByDomain(
                        domain,
                        withSubCodes,
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok().headers(referenceCodes.getPaginationHeaders()).body(referenceCodes.getItems());
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of reference codes for reference domain.", description = "List of reference codes for reference domain ordered by code ascending. The list is an un-paged flat list")
    @GetMapping("/domains/{domain}/codes")
    @SlowReportQuery
    public List<ReferenceCode> getReferenceCodesByDomain(@PathVariable("domain") @Parameter(description = "The domain identifier/name.", required = true) final String domain) {
        return referenceDomainService.getReferenceCodesByDomain(domain);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of all reference domains", description = "A reference domain can be used to retrieve all codes related to that domain. Ordered by domain ascending")
    @GetMapping("/domains")
    @SlowReportQuery
    public List<ReferenceDomain> getAllReferenceDomains() {
        return referenceDomainService.getAllDomains();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ReferenceCode.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Reference code detail for reference domain and code (with sub-codes).", description = "Reference code detail for reference domain and code (with sub-codes).")
    @GetMapping("/domains/{domain}/codes/{code}")
    @SlowReportQuery
    public ReferenceCode getReferenceCodeByDomainAndCode(@PathVariable("domain") @Parameter(description = "The domain identifier/name.", required = true) final String domain, @PathVariable("code") @Parameter(description = "The reference code.", required = true) final String code, @RequestParam(value = "withSubCodes", required = false, defaultValue = "false") @Parameter(description = "Specify whether or not to return the reference code with its associated sub-codes.") final boolean withSubCodes) {
        return referenceDomainService
                .getReferenceCodeByDomainAndCode(domain, code, withSubCodes).orElseThrow( () -> {

                    // If no exception thrown in service layer, we know that reference code exists for specified domain and code.
                    // However, if sub-codes were requested but reference code does not have any sub-codes, response from service
                    // layer will be empty - this is a bad request.

                    final var message = String.format("Reference code for domain [%s] and code [%s] does not have sub-codes.", domain, code);
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
                });

    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Reference code matching description ", description = "Wild card can be specified")
    @GetMapping("/domains/{domain}/reverse-lookup")
    @SlowReportQuery
    public List<ReferenceCode> getReferenceCodeByDomainAndDescription(@PathVariable("domain") @Parameter(description = "The domain identifier/name.", required = true) final String domain,
                                                                      @RequestParam(value = "description") @Parameter(description = "decription of a reference code to find", required = true) final String description,
                                                                      @RequestParam(value = "wildcard", required = false, defaultValue = "false") @Parameter(description = "Specify whether or not to wild card the results") final boolean wildcard) {
        return referenceDomainService.getReferenceCodeByDomainAndDescription(domain, description, wildcard);
    }


    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ReferenceCode.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Creates a reference code")
    @PostMapping("/domains/{domain}/codes/{code}")
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ReferenceCode createReferenceCode(@Size(max = 12) @NotNull @PathVariable("domain") @Parameter(description = "The domain identifier/name.", required = true) final String domain, @Size(max = 12) @NotNull @PathVariable("code") @Parameter(description = "The reference code.", required = true) final String code, @RequestBody @jakarta.validation.Valid @NotNull @Parameter(description = "Reference Information", required = true) final ReferenceCodeInfo referenceData) {
        return referenceDomainService.createReferenceCode(domain, code, referenceData);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ReferenceCode.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Updates a reference code")
    @PutMapping("/domains/{domain}/codes/{code}")
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ReferenceCode updateReferenceCode(@Size(max = 12) @NotNull @PathVariable("domain") @Parameter(description = "The domain identifier/name.", required = true) final String domain, @Size(max = 12) @NotNull @PathVariable("code") @Parameter(description = "The reference code.", required = true) final String code, @jakarta.validation.Valid @NotNull @RequestBody @Parameter(description = "Reference Information", required = true) final ReferenceCodeInfo referenceData) {
        return referenceDomainService.updateReferenceCode(domain, code, referenceData);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get possible reason codes for created event.", description = "Get possible reason codes for created event.")
    @GetMapping("/scheduleReasons")
    @SlowReportQuery
    public List<ReferenceCode> getScheduleReasons(@RequestParam("eventType") @Parameter(description = "Specify event type.", required = true) final String eventType) {
        return referenceDomainService.getScheduleReasons(eventType);
    }
}
