package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.KeyWorkerAllocationDetail;
import uk.gov.justice.hmpps.prison.api.model.Keyworker;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorker;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.keyworker.KeyWorkerAllocationService;

import java.util.Collections;
import java.util.List;

@RestController
@Tag(name = "key-worker")
@Validated
@RequestMapping(value = "${api.base.path}/key-worker", produces = "application/json")
public class KeyWorkerResource {
    private final KeyWorkerAllocationService keyWorkerService;

    public KeyWorkerResource(final KeyWorkerAllocationService keyWorkerService) {
        this.keyWorkerService = keyWorkerService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Key workers available for allocation at specified agency.", description = "Key workers available for allocation at specified agency.")
    @GetMapping("/{agencyId}/available")
    public List<Keyworker> getAvailableKeyworkers(@PathVariable("agencyId") @Parameter(description = "The agency (prison) identifier.", required = true) final String agencyId) {
        return keyWorkerService.getAvailableKeyworkers(agencyId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Specified key worker's currently assigned offenders.", description = "Specified key worker's currently assigned offenders.")
    @GetMapping("/{staffId}/agency/{agencyId}/offenders")
    public List<KeyWorkerAllocationDetail> getAllocationsForKeyworker(@PathVariable("staffId") @Parameter(description = "The key worker staff id", required = true) final Long staffId, @PathVariable("agencyId") @Parameter(description = "The agency (prison) identifier.", required = true) final String agencyId) {
        return keyWorkerService.getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The allocations list is returned.")})
    @Operation(summary = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", description = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.")
    @PostMapping("/{agencyId}/current-allocations")
    public List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocations(@PathVariable("agencyId") @Parameter(description = "The agency (prison) identifier.", required = true) final String agencyId, @RequestBody @Parameter(description = "The required staff Ids (mandatory)", required = true) final List<Long> staffIds) {
        return keyWorkerService.getAllocationDetailsForKeyworkers(staffIds, agencyId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The allocations list is returned.")})
    @Operation(summary = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", description = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.")
    @PostMapping("/{agencyId}/current-allocations/offenders")
    public List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocationsOffenders(@PathVariable("agencyId") @Parameter(description = "The agency (prison) identifier.", required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender Nos (mandatory)", required = true) final List<String> offenderNos) {
        return keyWorkerService.getAllocationDetailsForOffenders(offenderNos, agencyId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The allocations history list is returned.")})
    @Operation(summary = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", description = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.")
    @PostMapping("/offenders/allocationHistory")
    public List<OffenderKeyWorker> postKeyWorkerOffendersAllocationHistory(@RequestBody @Parameter(description = "The required offender nos (mandatory)", required = true) final List<String> offenderNos) {
        return keyWorkerService.getAllocationHistoryByOffenderNos(offenderNos);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The allocations history list is returned.")})
    @Operation(summary = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", description = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.")
    @PostMapping("/staff/allocationHistory")
    public List<OffenderKeyWorker> postKeyWorkerStaffAllocationHistory(@RequestBody @Parameter(description = "The required staff Ids (mandatory)", required = true) final List<Long> staffIds) {
        return keyWorkerService.getAllocationHistoryByStaffIds(staffIds);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "All allocations in specified agency.", description = "All allocations in specified agency.")
    @GetMapping("/{agencyId}/allocationHistory")
    public ResponseEntity<List<OffenderKeyWorker>> getAllocationHistory(@PathVariable("agencyId") @Parameter(description = "The agency (prison) identifier.", required = true) final String agencyId, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of allocationHistory records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of allocationHistory records returned.") final Long pageLimit) {
        final var pageRequest = new PageRequest(pageOffset, pageLimit);
        final var allocations = keyWorkerService.getAllocationHistoryByAgency(agencyId, pageRequest);

        return ResponseEntity.ok()
                .headers(allocations.getPaginationHeaders())
                .body(allocations.getItems());
    }
}
