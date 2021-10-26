package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRole;
import uk.gov.justice.hmpps.prison.api.model.StaffRole;
import uk.gov.justice.hmpps.prison.api.model.StaffUserRole;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.StaffService;
import uk.gov.justice.hmpps.prison.service.support.GetStaffRoleRequest;

import java.util.List;

@RestController
@Api(tags = {"staff"})
@Validated
@RequestMapping("${api.base.path}/staff")
public class StaffResource {
    private final String apiCaseloadId;
    private final StaffService staffService;

    public StaffResource(final StaffService staffService,
                         @Value("${application.caseload.id}") final String apiCaseloadId) {
        this.staffService = staffService;
        this.apiCaseloadId = apiCaseloadId;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Staff detail.", notes = "Staff detail.", nickname = "getStaffDetail")
    @GetMapping("/{staffId}")
    public StaffDetail getStaffDetail(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff member.", required = true) final Long staffId) {
        return staffService.getStaffDetail(staffId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "The staffId supplied was not valid.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 204, message = "No email addresses were found for this staff member."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Returns a list of email addresses associated with this staff user", notes = "List of email addresses for a specified staff user", nickname = "getStaffEmailAddresses")
    @GetMapping("/{staffId}/emails")
    public List<String> getStaffEmailAddresses(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff user.", required = true) final Long staffId) {
        return staffService.getStaffEmailAddresses(staffId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseLoad.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "The staffId supplied was not valid or not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 204, message = "No caseloads were found for this staff member."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Returns a list of caseloads associated with this staff user", notes = "List of caseloads for a specified staff user", nickname = "getStaffCaseloads")
    @GetMapping("/{staffId}/caseloads")
    public List<CaseLoad> getStaffCaseloads(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff user.", required = true, example = "123123") final Long staffId) {
        return staffService.getStaffCaseloads(staffId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffLocationRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get staff members within agency who are currently assigned the specified position and/or role.", notes = "Get staff members within agency who are currently assigned the specified position and/or role.", nickname = "getStaffByAgencyPositionRole")
    @GetMapping("/roles/{agencyId}/position/{position}/role/{role}")
    public ResponseEntity<List<StaffLocationRole>> getStaffByAgencyPositionRole(
            @PathVariable("agencyId") @ApiParam(value = "The agency (prison) id.", required = true) final String agencyId, @PathVariable("position") @ApiParam(value = "The staff position.", required = true) final String position, @PathVariable("role") @ApiParam(value = "The staff role.", required = true) final String role, @RequestParam(value = "nameFilter", required = false) @ApiParam("Filter results by first name and/or last name of staff member.") final String nameFilter, @RequestParam(value = "staffId", required = false) @ApiParam("The staff id of a staff member.") final Long staffId, @RequestParam(value = "activeOnly", defaultValue = "true", required = false) @ApiParam(value = "Filters results by activeOnly staff members.", defaultValue = "true") final Boolean activeOnly,
            @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of role records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of role records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {

        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, position, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return ResponseEntity.ok()
                .headers(staffDetails.getPaginationHeaders())
                .body(staffDetails.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffLocationRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get staff members within agency who are currently assigned the specified role.", notes = "Get staff members within agency who are currently assigned the specified role.", nickname = "getStaffByAgencyRole")
    @GetMapping("/roles/{agencyId}/role/{role}")
    public ResponseEntity<List<StaffLocationRole>> getStaffByAgencyRole(
            @PathVariable("agencyId") @ApiParam(value = "The agency (prison) id.", required = true) final String agencyId, @PathVariable("role") @ApiParam(value = "The staff role.", required = true) final String role, @RequestParam(value = "nameFilter", required = false) @ApiParam("Filter results by first name and/or last name of staff member. Supplied filter term is matched to start of staff member's first and last name.") final String nameFilter, @RequestParam(value = "staffId", required = false) @ApiParam("The staff id of a staff member.") final Long staffId, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @ApiParam(value = "Filters results by activeOnly staff members.", defaultValue = "true") final Boolean activeOnly,
            @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of role records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of role records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {

        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, null, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return ResponseEntity.ok()
                .headers(staffDetails.getPaginationHeaders())
                .body(staffDetails.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffUserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of access roles for specified staff user and caseload", notes = "List of access roles for specified staff user and caseload", nickname = "getStaffAccessRoles")
    @GetMapping("/{staffId}/access-roles")
    public List<StaffUserRole> getStaffAccessRoles(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff user.", required = true) final Long staffId) {
        return staffService.getStaffRoles(staffId);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffUserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of access roles for specified staff user and caseload", notes = "List of access roles for specified staff user and caseload", nickname = "getAccessRolesByCaseload")
    @GetMapping("/{staffId}/access-roles/caseload/{caseload}")
    public List<StaffUserRole> getAccessRolesByCaseload(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff member.", required = true) final Long staffId, @PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload) {
        return staffService.getRolesByCaseload(staffId, caseload);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of job roles for specified staff and agency Id", notes = "List of job roles for specified staff and agency Id", nickname = "getAllRolesForAgency")
    @GetMapping("/{staffId}/{agencyId}/roles")
    public List<StaffRole>  getAllRolesForAgency(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff member.", required = true) final Long staffId, @PathVariable("agencyId") @ApiParam(value = "Agency Id.", required = true) final String agencyId) {
        return staffService.getAllRolesForAgency(staffId, agencyId);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "The access role has been created.", response = StaffUserRole.class)})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "add access role to staff user for API caseload", notes = "add access role to staff user for API caseload", nickname = "addStaffAccessRoleForApiCaseload")
    @PostMapping("/{staffId}/access-roles")
    @ProxyUser
    @Hidden
    public StaffUserRole addStaffAccessRoleForApiCaseload(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff user.", required = true) final Long staffId, @RequestBody @ApiParam(value = "new access role code required", required = true) final String body) {
        return staffService.addStaffRole(staffId, apiCaseloadId, body);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "The access role has been created.", response = StaffUserRole.class)})
    @ApiOperation(value = "add access role to staff user", notes = "add access role to staff user", nickname = "addStaffAccessRole")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{staffId}/access-roles/caseload/{caseload}")
    @ProxyUser
    @Hidden
    public StaffUserRole addStaffAccessRole(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff member.", required = true) final Long staffId, @PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload, @RequestBody @ApiParam(value = "new access role code required", required = true) final String body) {
        return staffService.addStaffRole(staffId, caseload, body);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The access role has been removed", response = String.class)})
    @ApiOperation(value = "remove access roles from user and specific caseload", notes = "remove access roles from user and specific caseload", nickname = "removeStaffAccessRole")
    @DeleteMapping("/{staffId}/access-roles/caseload/{caseload}/access-role/{roleCode}")
    @ProxyUser
    @Hidden
    public ResponseEntity<Void> removeStaffAccessRole(@PathVariable("staffId") @ApiParam(value = "The staff id of the staff member.", required = true) final Long staffId, @PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload, @PathVariable("roleCode") @ApiParam(value = "access role code", required = true) final String roleCode) {
        staffService.removeStaffRole(staffId, caseload, roleCode);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = StaffUserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List access roles for staff by type and caseload", notes = "List access roles for staff by type and caseload", nickname = "getAllStaffAccessRolesForCaseload")
    @GetMapping("/access-roles/caseload/{caseload}/access-role/{roleCode}")
    public List<StaffUserRole> getAllStaffAccessRolesForCaseload(@PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload, @PathVariable("roleCode") @ApiParam(value = "access role code", required = true) final String roleCode) {
        return staffService.getAllStaffRolesForCaseload(caseload, roleCode);
    }
}
