package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"/key-worker"})
@SuppressWarnings("unused")
public interface KeyWorkerResource {

    @GetMapping("/{agencyId}/allocationHistory")
    @ApiOperation(value = "All allocations in specified agency.", notes = "All allocations in specified agency.", nickname = "getAllocationHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderKeyWorker.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<OffenderKeyWorker>> getAllocationHistory(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathVariable("agencyId") String agencyId,
                                                                 @ApiParam(value = "Requested offset of first record in returned collection of allocationHistory records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                 @ApiParam(value = "Requested limit to number of allocationHistory records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit);

    @GetMapping("/{agencyId}/available")
    @ApiOperation(value = "Key workers available for allocation at specified agency.", notes = "Key workers available for allocation at specified agency.", nickname = "getAvailableKeyworkers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Keyworker.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Keyworker> getAvailableKeyworkers(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathVariable("agencyId") String agencyId);

    @GetMapping("/{staffId}/agency/{agencyId}/offenders")
    @ApiOperation(value = "Specified key worker's currently assigned offenders.", notes = "Specified key worker's currently assigned offenders.", nickname = "getAllocationsForKeyworker")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = KeyWorkerAllocationDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<KeyWorkerAllocationDetail> getAllocationsForKeyworker(@ApiParam(value = "The key worker staff id", required = true) @PathVariable("staffId") Long staffId,
                                                               @ApiParam(value = "The agency (prison) identifier.", required = true) @PathVariable("agencyId") String agencyId);

    @PostMapping("/{agencyId}/current-allocations")
    @ApiOperation(value = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", nickname = "postKeyWorkerAgencyIdCurrentAllocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations list is returned.", response = KeyWorkerAllocationDetail.class, responseContainer = "List")})
    List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocations(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathVariable("agencyId") String agencyId,
                                                                            @ApiParam(value = "The required staff Ids (mandatory)", required = true) @RequestBody List<Long> body);

    @PostMapping("/{agencyId}/current-allocations/offenders")
    @ApiOperation(value = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", nickname = "postKeyWorkerAgencyIdCurrentAllocationsOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations list is returned.", response = KeyWorkerAllocationDetail.class, responseContainer = "List")})
    List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocationsOffenders(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathVariable("agencyId") String agencyId,
                                                                                     @ApiParam(value = "The required offender Nos (mandatory)", required = true) @RequestBody List<String> body);

    @PostMapping("/offenders/allocationHistory")
    @ApiOperation(value = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", notes = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", nickname = "postKeyWorkerOffendersAllocationHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations history list is returned.", response = OffenderKeyWorker.class, responseContainer = "List")})
    List<OffenderKeyWorker> postKeyWorkerOffendersAllocationHistory(@ApiParam(value = "The required offender nos (mandatory)", required = true) @RequestBody List<String> body);

    @PostMapping("/staff/allocationHistory")
    @ApiOperation(value = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", nickname = "postKeyWorkerStaffAllocationHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations history list is returned.", response = OffenderKeyWorker.class, responseContainer = "List")})
    List<OffenderKeyWorker> postKeyWorkerStaffAllocationHistory(@ApiParam(value = "The required staff Ids (mandatory)", required = true) @RequestBody List<Long> body);


}
