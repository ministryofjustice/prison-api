package net.syscon.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.prison.api.model.CaseLoad;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.StaffDetail;
import net.syscon.prison.api.model.StaffLocationRole;
import net.syscon.prison.api.model.StaffRole;
import net.syscon.prison.api.model.StaffUserRole;
import net.syscon.prison.api.support.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Api(tags = {"/staff"})
@SuppressWarnings("unused")
public interface StaffResource {

    @DeleteMapping("/{staffId}/access-roles/caseload/{caseload}/access-role/{roleCode}")
    @ApiOperation(value = "remove access roles from user and specific caseload", notes = "remove access roles from user and specific caseload", nickname = "removeStaffAccessRole")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The access role has been removed", response = String.class)})
    ResponseEntity<Void> removeStaffAccessRole(@ApiParam(value = "The staff id of the staff member.", required = true) @PathVariable("staffId") Long staffId,
                                                        @ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                        @ApiParam(value = "access role code", required = true) @PathVariable("roleCode") String roleCode);

    @GetMapping("/access-roles/caseload/{caseload}/access-role/{roleCode}")
    @ApiOperation(value = "List access roles for staff by type and caseload", notes = "List access roles for staff by type and caseload", nickname = "getAllStaffAccessRolesForCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffUserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<StaffUserRole> getAllStaffAccessRolesForCaseload(@ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                                                @ApiParam(value = "access role code", required = true) @PathVariable("roleCode") String roleCode);

    @GetMapping("/roles/{agencyId}/position/{position}/role/{role}")
    @ApiOperation(value = "Get staff members within agency who are currently assigned the specified position and/or role.", notes = "Get staff members within agency who are currently assigned the specified position and/or role.", nickname = "getStaffByAgencyPositionRole")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffLocationRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<StaffLocationRole>> getStaffByAgencyPositionRole(@ApiParam(value = "The agency (prison) id.", required = true) @PathVariable("agencyId") String agencyId,
                                                                         @ApiParam(value = "The staff position.", required = true) @PathVariable("position") String position,
                                                                         @ApiParam(value = "The staff role.", required = true) @PathVariable("role") String role,
                                                                         @ApiParam(value = "Filter results by first name and/or last name of staff member.") @RequestParam(value = "nameFilter", required = false) String nameFilter,
                                                                         @ApiParam(value = "The staff id of a staff member.") @RequestParam(value = "staffId", required = false) Long staffId,
                                                                         @ApiParam(value = "Filters results by activeOnly staff members.", defaultValue = "true") @RequestParam(value = "activeOnly", defaultValue = "true", required = false) Boolean activeOnly,
                                                                         @ApiParam(value = "Requested offset of first record in returned collection of role records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                         @ApiParam(value = "Requested limit to number of role records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                         @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                         @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/roles/{agencyId}/role/{role}")
    @ApiOperation(value = "Get staff members within agency who are currently assigned the specified role.", notes = "Get staff members within agency who are currently assigned the specified role.", nickname = "getStaffByAgencyRole")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffLocationRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<StaffLocationRole>> getStaffByAgencyRole(@ApiParam(value = "The agency (prison) id.", required = true) @PathVariable("agencyId") String agencyId,
                                                      @ApiParam(value = "The staff role.", required = true) @PathVariable("role") String role,
                                                      @ApiParam(value = "Filter results by first name and/or last name of staff member. Supplied filter term is matched to start of staff member's first and last name.") @RequestParam(value = "nameFilter", required = false) String nameFilter,
                                                      @ApiParam(value = "The staff id of a staff member.") @RequestParam(value = "staffId", required = false) Long staffId,
                                                      @ApiParam(value = "Filters results by activeOnly staff members.", defaultValue = "true") @RequestParam(value = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly,
                                                      @ApiParam(value = "Requested offset of first record in returned collection of role records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                      @ApiParam(value = "Requested limit to number of role records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{staffId}")


    @ApiOperation(value = "Staff detail.", notes = "Staff detail.", nickname = "getStaffDetail")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    StaffDetail getStaffDetail(@ApiParam(value = "The staff id of the staff member.", required = true) @PathVariable("staffId") Long staffId);

    @GetMapping("/{staffId}/emails")
    @ApiOperation(value = "Returns a list of email addresses associated with this staff user", notes = "List of email addresses for a specified staff user", nickname = "getStaffEmailAddresses")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "The staffId supplied was not valid.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 204, message = "No email addresses were found for this staff member."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<String> getStaffEmailAddresses(@ApiParam(value = "The staff id of the staff user.", required = true) @PathVariable("staffId") Long staffId);

    @GetMapping("/{staffId}/caseloads")
    @ApiOperation(value = "Returns a list of caseloads associated with this staff user", notes = "List of caseloads for a specified staff user", nickname = "getStaffCaseloads")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseLoad.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "The staffId supplied was not valid or not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 204, message = "No caseloads were found for this staff member."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<CaseLoad> getStaffCaseloads(@ApiParam(value = "The staff id of the staff user.", required = true, example = "123123") @PathVariable("staffId") Long staffId);


    @GetMapping("/{staffId}/access-roles")
    @ApiOperation(value = "List of access roles for specified staff user and caseload", notes = "List of access roles for specified staff user and caseload", nickname = "getStaffAccessRoles")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffUserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<StaffUserRole> getStaffAccessRoles(@ApiParam(value = "The staff id of the staff user.", required = true) @PathVariable("staffId") Long staffId);

    @GetMapping("/{staffId}/access-roles/caseload/{caseload}")
    @ApiOperation(value = "List of access roles for specified staff user and caseload", notes = "List of access roles for specified staff user and caseload", nickname = "getAccessRolesByCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffUserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<StaffUserRole> getAccessRolesByCaseload(@ApiParam(value = "The staff id of the staff member.", required = true) @PathVariable("staffId") Long staffId,
                                                              @ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload);

    @GetMapping("/{staffId}/{agencyId}/roles")
    @ApiOperation(value = "List of job roles for specified staff and agency Id", notes = "List of job roles for specified staff and agency Id", nickname = "getAllRolesForAgency")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<StaffRole> getAllRolesForAgency(@ApiParam(value = "The staff id of the staff member.", required = true) @PathVariable("staffId") Long staffId,
                                                      @ApiParam(value = "Agency Id.", required = true) @PathVariable("agencyId") String agencyId);

    @PostMapping("/{staffId}/access-roles")
    @ApiOperation(value = "add access role to staff user for API caseload", notes = "add access role to staff user for API caseload", nickname = "addStaffAccessRoleForApiCaseload")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The access role has been created.", response = StaffUserRole.class)})
    StaffUserRole addStaffAccessRoleForApiCaseload(@ApiParam(value = "The staff id of the staff user.", required = true) @PathVariable("staffId") Long staffId,
                                                                              @ApiParam(value = "new access role code required", required = true) @RequestBody String body);

    @PostMapping("/{staffId}/access-roles/caseload/{caseload}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "add access role to staff user", notes = "add access role to staff user", nickname = "addStaffAccessRole")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The access role has been created.", response = StaffUserRole.class)})
    StaffUserRole addStaffAccessRole(@ApiParam(value = "The staff id of the staff member.", required = true) @PathVariable("staffId") Long staffId,
                                                  @ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                  @ApiParam(value = "new access role code required", required = true) @RequestBody String body);

}
