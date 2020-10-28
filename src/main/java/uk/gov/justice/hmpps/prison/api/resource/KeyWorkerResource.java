package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Validated
@RequestMapping("${api.base.path}/key-worker")
public class KeyWorkerResource {
    private final KeyWorkerAllocationService keyWorkerService;

    public KeyWorkerResource(final KeyWorkerAllocationService keyWorkerService) {
        this.keyWorkerService = keyWorkerService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Keyworker.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Key workers available for allocation at specified agency.", notes = "Key workers available for allocation at specified agency.", nickname = "getAvailableKeyworkers")
    @GetMapping("/{agencyId}/available")
    public List<Keyworker> getAvailableKeyworkers(@PathVariable("agencyId") @ApiParam(value = "The agency (prison) identifier.", required = true) final String agencyId) {
        return keyWorkerService.getAvailableKeyworkers(agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = KeyWorkerAllocationDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Specified key worker's currently assigned offenders.", notes = "Specified key worker's currently assigned offenders.", nickname = "getAllocationsForKeyworker")
    @GetMapping("/{staffId}/agency/{agencyId}/offenders")
    public List<KeyWorkerAllocationDetail> getAllocationsForKeyworker(@PathVariable("staffId") @ApiParam(value = "The key worker staff id", required = true) final Long staffId, @PathVariable("agencyId") @ApiParam(value = "The agency (prison) identifier.", required = true) final String agencyId) {
        return keyWorkerService.getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The allocations list is returned.", response = KeyWorkerAllocationDetail.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", nickname = "postKeyWorkerAgencyIdCurrentAllocations")
    @PostMapping("/{agencyId}/current-allocations")
    public List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocations(@PathVariable("agencyId") @ApiParam(value = "The agency (prison) identifier.", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required staff Ids (mandatory)", required = true) final List<Long> staffIds) {
        return keyWorkerService.getAllocationDetailsForKeyworkers(staffIds, agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The allocations list is returned.", response = KeyWorkerAllocationDetail.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", nickname = "postKeyWorkerAgencyIdCurrentAllocationsOffenders")
    @PostMapping("/{agencyId}/current-allocations/offenders")
    public List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocationsOffenders(@PathVariable("agencyId") @ApiParam(value = "The agency (prison) identifier.", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender Nos (mandatory)", required = true) final List<String> offenderNos) {
        return keyWorkerService.getAllocationDetailsForOffenders(offenderNos, agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The allocations history list is returned.", response = OffenderKeyWorker.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", notes = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", nickname = "postKeyWorkerOffendersAllocationHistory")
    @PostMapping("/offenders/allocationHistory")
    public List<OffenderKeyWorker> postKeyWorkerOffendersAllocationHistory(@RequestBody @ApiParam(value = "The required offender nos (mandatory)", required = true) final List<String> offenderNos) {
        return keyWorkerService.getAllocationHistoryByOffenderNos(offenderNos);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The allocations history list is returned.", response = OffenderKeyWorker.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", nickname = "postKeyWorkerStaffAllocationHistory")
    @PostMapping("/staff/allocationHistory")
    public List<OffenderKeyWorker> postKeyWorkerStaffAllocationHistory(@RequestBody @ApiParam(value = "The required staff Ids (mandatory)", required = true) final List<Long> staffIds) {
        return keyWorkerService.getAllocationHistoryByStaffIds(staffIds);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderKeyWorker.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "All allocations in specified agency.", notes = "All allocations in specified agency.", nickname = "getAllocationHistory")
    @GetMapping("/{agencyId}/allocationHistory")
    public ResponseEntity<List<OffenderKeyWorker>> getAllocationHistory(@PathVariable("agencyId") @ApiParam(value = "The agency (prison) identifier.", required = true) final String agencyId, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of allocationHistory records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of allocationHistory records returned.", defaultValue = "10") final Long pageLimit) {
        final var pageRequest = new PageRequest(pageOffset, pageLimit);
        final var allocations = keyWorkerService.getAllocationHistoryByAgency(agencyId, pageRequest);

        return ResponseEntity.ok()
                .headers(allocations.getPaginationHeaders())
                .body(allocations.getItems());
    }
}
