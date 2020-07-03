package net.syscon.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.prison.api.model.AccessRole;
import net.syscon.prison.api.model.CaseLoad;
import net.syscon.prison.api.model.CaseloadUpdate;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.Location;
import net.syscon.prison.api.model.ReferenceCode;
import net.syscon.prison.api.model.StaffDetail;
import net.syscon.prison.api.model.UserDetail;
import net.syscon.prison.api.model.UserRole;
import net.syscon.prison.api.support.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Api(tags = {"/users"})
public interface UserResource {

    @DeleteMapping("/{username}/caseload/{caseload}/access-role/{roleCode}")
    @ApiOperation(value = "Remove the given access role from the user.", notes = "Remove the given access role from the user.", nickname = "removeUsersAccessRoleForCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User role has been removed"),
            @ApiResponse(code = 404, message = "The role is not recognised or user does not have role on caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles")})
    ResponseEntity<Void> removeUsersAccessRoleForCaseload(@ApiParam(value = "The username of the user.", required = true) @PathVariable("username") String username,
                                                                              @ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                                              @ApiParam(value = "access role code", required = true) @PathVariable("roleCode") String roleCode);

    @GetMapping
    @ApiOperation(value = "Get user details.", notes = "Get user details.", nickname = "getUsers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<UserDetail>> getUsers(@ApiParam(value = "Filter results by first name and/or username and/or last name of staff member.") @RequestParam(value = "nameFilter", required = false) String nameFilter,
                              @ApiParam(value = "Filter results by access role") @RequestParam(value = "accessRole", required = false) String accessRole,
                              @ApiParam(value = "Requested offset of first record in returned collection of user records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                              @ApiParam(value = "Requested limit to number of user records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                              @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                              @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/access-roles/caseload/{caseload}/access-role/{roleCode}")
    @ApiOperation(value = "List of users who have the named role at the named caseload", notes = "List of users who have the named role at the named caseload", nickname = "getAllUsersHavingRoleAtCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    Set<String> getAllUsersHavingRoleAtCaseload(@ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                                            @ApiParam(value = "access role code", required = true) @PathVariable("roleCode") String roleCode);

    @GetMapping("/caseload/{caseload}")
    @ApiOperation(value = "Get user details by prison.", notes = "Get user details by prison.", nickname = "getUsersByCaseLoad")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<UserDetail>>  getUsersByCaseLoad(@ApiParam(value = "The agency (prison) id.", required = true) @PathVariable("caseload") String caseload,
                                                  @ApiParam(value = "Filter results by first name and/or username and/or last name of staff member.") @RequestParam(value = "nameFilter", required = false) String nameFilter,
                                                  @ApiParam(value = "Filter results by access role") @RequestParam(value = "accessRole", required = false) String accessRole,
                                                  @ApiParam(value = "Requested offset of first record in returned collection of caseload records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                  @ApiParam(value = "Requested limit to number of caseload records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/local-administrator/caseload/{caseload}")
    @ApiOperation(value = "Deprecated: Get staff details for local administrators", notes = "Deprecated: please use /users/local-administrator/available", nickname = "getStaffUsersForLocalAdministrator")
    @Deprecated
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<UserDetail>> deprecatedPleaseRemove(@ApiParam(value = "The agency (prison) id.", required = true, allowEmptyValue = true) @PathVariable(value = "caseload") String caseload,
                                                             @ApiParam(value = "Filter results by first name and/or username and/or last name of staff member.") @RequestParam(value = "nameFilter", required = false) String nameFilter,
                                                             @ApiParam(value = "Filter results by access role") @RequestParam(value = "accessRole", required = false) String accessRole,
                                                             @ApiParam(value = "Requested offset of first record in returned collection of caseload records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                             @ApiParam(value = "Requested limit to number of caseload records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                             @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                             @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/local-administrator/available")
    @ApiOperation(value = "Get staff details for local administrator", notes = "Get user details for local administrator", nickname = "getStaffUsersForLocalAdministrator")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<UserDetail>> getStaffUsersForLocalAdministrator(@ApiParam(value = "Filter results by first name and/or username and/or last name of staff member.") @RequestParam(value = "nameFilter", required = false) String nameFilter,
                                                                         @ApiParam(value = "Filter results by access role") @RequestParam(value = "accessRole", required = false) String accessRole,
                                                                         @ApiParam(value = "Requested offset of first record in returned collection of caseload records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                         @ApiParam(value = "Requested limit to number of caseload records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                         @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                         @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/me")
    @ApiOperation(value = "Current user detail.", notes = "Current user detail.", nickname = "getMyUserInformation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    UserDetail getMyUserInformation();

    @GetMapping("/me/caseLoads")
    @ApiOperation(value = "List of caseloads accessible to current user.", notes = "List of caseloads accessible to current user.", nickname = "getMyCaseLoads")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseLoad.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<CaseLoad> getMyCaseLoads(@ApiParam(value = "If set to true then all caseloads are returned", defaultValue = "false") @RequestParam(value = "allCaseloads", required = false, defaultValue = "false") boolean allCaseloads);

    @GetMapping("/me/caseNoteTypes")
    @ApiOperation(value = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", notes = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", nickname = "getMyCaseNoteTypes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ReferenceCode> getMyCaseNoteTypes(@ApiParam(value = "Comma separated list of one or more of the following fields - <b>code, activeFlag, description</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/me/locations")
    @ApiOperation(value = "List of locations accessible to current user.", notes = "List of locations accessible to current user.", nickname = "getMyLocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Location> getMyLocations();

    @GetMapping("/me/roles")
    @ApiOperation(value = "List of roles for current user.", notes = "List of roles for current user.", nickname = "getMyRoles")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<UserRole> getMyRoles(@ApiParam(value = "If set to true then all roles are returned rather than just API roles", defaultValue = "false") @RequestParam(value = "allRoles", required = false, defaultValue = "false") boolean allRoles);

    @GetMapping("/staff/{staffId}")
    @Deprecated
    @ApiOperation(value = "Staff detail.", notes = "Deprecated: Use <b>/staff/{staffId}</b> instead. This API will be removed in a future release.", nickname = "getStaffDetail")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    StaffDetail getStaffDetail(@ApiParam(value = "The staff id of the staff member.", required = true) @PathVariable("staffId") Long staffId);

    @GetMapping("/{username}")
    @ApiOperation(value = "User detail.", notes = "User detail.", nickname = "getUserDetails")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    UserDetail getUserDetails(@ApiParam(value = "The username of the user.", required = true) @PathVariable("username") String username);


    @PostMapping("/list")
    @ApiOperation(value = "Returns the user details for supplied usernames - POST version to allow large user lists.", notes = "user details for supplied usernames", nickname = "getUserDetailsList")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of user details", response = UserDetail.class, responseContainer = "List")})
    List<UserDetail> getUserDetailsList(@ApiParam(value = "The required usernames (mandatory)", required = true) @RequestBody Set<String> body);


    @GetMapping("/{username}/access-roles/caseload/{caseload}")
    @ApiOperation(value = "List of roles for the given user and caseload", notes = "List of roles for the given user and caseload", nickname = "getRolesForUserAndCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AccessRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<AccessRole> getRolesForUserAndCaseload(@ApiParam(value = "user account to filter by", required = true) @PathVariable("username") String username,
                                                                  @ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                                  @ApiParam(value = "Include admin roles", required = true, defaultValue = "false") @RequestParam(value = "includeAdmin", defaultValue = "false", required = false) boolean includeAdmin);

    @PutMapping("/add/default/{caseload}")
    @ApiOperation(value = "Add the NWEB caseload to specified caseload.", notes = "Add the NWEB caseload to specified caseload.", nickname = "addApiAccessForCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "No New Users", response = CaseloadUpdate.class),
            @ApiResponse(code = 201, message = "New Users Enabled", response = CaseloadUpdate.class),
    })
    ResponseEntity<CaseloadUpdate> addApiAccessForCaseload(@ApiParam(value = "The caseload (equates to prison) id to add all active users to default API caseload (NWEB)", required = true) @PathVariable("caseload") String caseload);

    @PutMapping("/me/activeCaseLoad")
    @ApiOperation(value = "Update working caseload for current user.", notes = "Update working caseload for current user.", nickname = "updateMyActiveCaseLoad")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Invalid username or password", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "the user does not have permission to view the caseload.", response = ErrorResponse.class)})
    ResponseEntity<?> updateMyActiveCaseLoad(@ApiParam(value = "", required = true) @RequestBody CaseLoad body);

    @PutMapping("/{username}/access-role/{roleCode}")
    @ApiOperation(value = "Add the given access role to the user.", notes = "Add the given access role to the user.", nickname = "addAccessRole")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User already has role"),
            @ApiResponse(code = 201, message = "Role has been successfully added to user"),
            @ApiResponse(code = 404, message = "The role is not recognised or user cannot access caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles"),
    })
    ResponseEntity<Void> addAccessRole(@ApiParam(value = "The username of the user.", required = true) @PathVariable("username") String username,
                                        @ApiParam(value = "access role code", required = true) @PathVariable("roleCode") String roleCode);

    @PutMapping("/{username}/caseload/{caseload}/access-role/{roleCode}")
    @ApiOperation(value = "Add the given access role to the user and caseload.", notes = "Add the given access role to the user and caseload.", nickname = "addAccessRoleByCaseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User already has role"),
            @ApiResponse(code = 201, message = "Role has been successfully added to user"),
            @ApiResponse(code = 404, message = "The role is not recognised or user cannot access caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles")})
    ResponseEntity<Void> addAccessRoleByCaseload(@ApiParam(value = "The username of the user.", required = true) @PathVariable("username") String username,
                                                            @ApiParam(value = "Caseload Id", required = true) @PathVariable("caseload") String caseload,
                                                            @ApiParam(value = "access role code", required = true) @PathVariable("roleCode") String roleCode);

}
