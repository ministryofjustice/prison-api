package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;


@RestController
@RequestMapping("${api.base.path}/reference-domains")
@Validated
public class ReferenceDomainResource {
    private final ReferenceDomainService referenceDomainService;
    private final CaseNoteService caseNoteService;

    public ReferenceDomainResource(final ReferenceDomainService referenceDomainService, final CaseNoteService caseNoteService) {
        this.referenceDomainService = referenceDomainService;
        this.caseNoteService = caseNoteService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of alert types (with alert codes).", notes = "List of alert types (with alert codes).", nickname = "getAlertTypes")
    @GetMapping("/alertTypes")
    public ResponseEntity<List<ReferenceCode>> getAlertTypes(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of alertType records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of alertType records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>code, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
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
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of case note source codes.", notes = "List of case note source codes.", nickname = "getCaseNoteSources")
    @GetMapping("/caseNoteSources")
    public ResponseEntity<List<ReferenceCode>> getCaseNoteSources(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of caseNoteSource records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of caseNoteSource records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>code, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var caseNoteSources =
                referenceDomainService.getCaseNoteSources(
                        sortFields,
                        sortOrder,
                        nvl(pageOffset, 0L),
                        nvl(pageLimit, 10L));

        return ResponseEntity.ok().headers(caseNoteSources.getPaginationHeaders()).body(caseNoteSources.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of all used case note types (with sub-types).", notes = "List of all used case note types (with sub-types).", nickname = "getCaseNoteTypes")
    @GetMapping("/caseNoteTypes")
    public  List<ReferenceCode> getCaseNoteTypes() {
        return caseNoteService.getUsedCaseNoteTypesWithSubTypes();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of reference codes for reference domain.", notes = "List of reference codes for reference domain.", nickname = "getReferenceCodesByDomain")
    @GetMapping("/domains/{domain}")
    public ResponseEntity<List<ReferenceCode>> getReferenceCodesByDomain(@PathVariable("domain") @ApiParam(value = "The domain identifier/name.", required = true) final String domain, @RequestParam(value = "withSubCodes", required = false, defaultValue = "false") @ApiParam(value = "Specify whether or not to return reference codes with their associated sub-codes.", defaultValue = "false") final boolean withSubCodes, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of domain records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of domain records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>code, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
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
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Reference code detail for reference domain and code (with sub-codes).", notes = "Reference code detail for reference domain and code (with sub-codes).", nickname = "getReferenceCodeByDomainAndCode")
    @GetMapping("/domains/{domain}/codes/{code}")
    public ReferenceCode getReferenceCodeByDomainAndCode(@PathVariable("domain") @ApiParam(value = "The domain identifier/name.", required = true) final String domain, @PathVariable("code") @ApiParam(value = "The reference code.", required = true) final String code, @RequestParam(value = "withSubCodes", required = false, defaultValue = "false") @ApiParam(value = "Specify whether or not to return the reference code with its associated sub-codes.", defaultValue = "false") final boolean withSubCodes) {
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
            @ApiResponse(code = 201, message = "Created", response = ReferenceCode.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Creates a reference code")
    @PostMapping("/domains/{domain}/codes/{code}")
    @HasWriteScope
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA','SYSTEM_USER')")
    @ProxyUser
    public ReferenceCode createReferenceCode(@Size(max = 12) @NotNull @PathVariable("domain") @ApiParam(value = "The domain identifier/name.", required = true) final String domain, @Size(max = 12) @NotNull @PathVariable("code") @ApiParam(value = "The reference code.", required = true) final String code, @RequestBody @javax.validation.Valid @NotNull @ApiParam(value = "Reference Information", required = true) final ReferenceCodeInfo referenceData) {
        return referenceDomainService.createReferenceCode(domain, code, referenceData);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated", response = ReferenceCode.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Updates a reference code")
    @PutMapping("/domains/{domain}/codes/{code}")
    @HasWriteScope
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA','SYSTEM_USER')")
    @ProxyUser
    public ReferenceCode updateReferenceCode(@Size(max = 12) @NotNull @PathVariable("domain") @ApiParam(value = "The domain identifier/name.", required = true) final String domain, @Size(max = 12) @NotNull @PathVariable("code") @ApiParam(value = "The reference code.", required = true) final String code, @javax.validation.Valid @NotNull @RequestBody @ApiParam(value = "Reference Information", required = true) final ReferenceCodeInfo referenceData) {
        return referenceDomainService.updateReferenceCode(domain, code, referenceData);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get possible reason codes for created event.", notes = "Get possible reason codes for created event.", nickname = "getScheduleReasons")
    @GetMapping("/scheduleReasons")
    public List<ReferenceCode> getScheduleReasons(@RequestParam("eventType") @ApiParam(value = "Specify event type.", required = true) final String eventType) {
        return referenceDomainService.getScheduleReasons(eventType);
    }


}
