package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.CaseloadUpdate;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.CaseLoadService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.LocationService;
import uk.gov.justice.hmpps.prison.service.UserService;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

@RestController
@Api(tags = {"users"})
@Validated
@RequestMapping("${api.base.path}/users")
public class UserResource {
    private final AuthenticationFacade authenticationFacade;
    private final UserService userService;
    private final LocationService locationService;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;

    public UserResource(final AuthenticationFacade authenticationFacade,
                        final LocationService locationService,
                        final UserService userService,
                        final CaseLoadService caseLoadService,
                        final CaseNoteService caseNoteService) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.userService = userService;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get staff details for local administrator", notes = "Get user details for local administrator", nickname = "getStaffUsersForLocalAdministrator")
    @GetMapping("/local-administrator/available")
    public ResponseEntity<List<UserDetail>> getStaffUsersForLocalAdministrator(@RequestParam(value = "nameFilter", required = false) @ApiParam("Filter results by first name and/or username and/or last name of staff member.") final String nameFilter,
                                                                               @RequestParam(value = "accessRole", required = false) @ApiParam("Filter results by access role") final List<String> accessRoles,
                                                                               @RequestParam(value = "status", required = false, defaultValue = "ALL") @ApiParam("Limit to active / inactive / show all users.") final Status status,
                                                                               @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of caseload records.", defaultValue = "0") final Long pageOffset,
                                                                               @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of caseload records returned.", defaultValue = "10") final Long pageLimit,
                                                                               @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields,
                                                                               @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsersAsLocalAdministrator(authenticationFacade.getCurrentUsername(), nameFilter, accessRoles, status, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "User role has been removed"),
            @ApiResponse(code = 404, message = "The role is not recognised or user does not have role on caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles")})
    @ApiOperation(value = "Remove the given access role from the user.", notes = "Remove the given access role from the user.", nickname = "removeUsersAccessRoleForCaseload")
    @DeleteMapping("/{username}/caseload/{caseload}/access-role/{roleCode}")
    @ProxyUser
    public ResponseEntity<Void> removeUsersAccessRoleForCaseload(@PathVariable("username") @ApiParam(value = "The username of the user.", required = true) final String username, @PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload, @PathVariable("roleCode") @ApiParam(value = "access role code", required = true) final String roleCode) {
        userService.removeUsersAccessRoleForCaseload(username, caseload, roleCode);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = UserDetail.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get user details.", notes = "Get user details.", nickname = "getUsers")
    @GetMapping
    public ResponseEntity<List<UserDetail>> getUsers(@RequestParam(value = "nameFilter", required = false) @ApiParam("Filter results by first name and/or username and/or last name of staff member.") final String nameFilter,
                                                     @RequestParam(value = "accessRole", required = false) @ApiParam("Filter results by access roles") final List<String> accessRoles,
                                                     @RequestParam(value = "status", required = false, defaultValue = "ALL") @ApiParam("Limit to active / inactive / show all users.") final Status status,
                                                     @RequestParam(value = "caseload", required = false) @ApiParam(value = "Filter results to include only those users that have access to the specified caseload (irrespective of whether it is currently active or not", example = "MDI") final String caseload,
                                                     @RequestParam(value = "activeCaseload", required = false) @ApiParam(value = "Filter results by user's currently active caseload i.e. the one they have currently selected.", example = "MDI") final String activeCaseload,
                                                     @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false)
                                                         @ApiParam(value = "Requested offset of first record in returned collection of user records.", defaultValue = "0") final Long pageOffset,
                                                     @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false)
                                                         @ApiParam(value = "Requested limit to number of user records returned.", defaultValue = "10") final Long pageLimit,
                                                     @RequestHeader(value = "Sort-Fields", required = false)
                                                         @ApiParam("Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields,
                                                     @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false)
                                                         @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsers(nameFilter, accessRoles, status,
            StringUtils.trimToNull(caseload), StringUtils.trimToNull(activeCaseload), pageRequest);

        return ResponseEntity.ok()
            .headers(userDetails.getPaginationHeaders())
            .body(userDetails.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "User already has role"),
            @ApiResponse(code = 201, message = "Role has been successfully added to user"),
            @ApiResponse(code = 404, message = "The role is not recognised or user cannot access caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles"),
    })
    @ApiOperation(value = "Add the given access role to the user.", notes = "Add the given access role to the user.", nickname = "addAccessRole")
    @PutMapping("/{username}/access-role/{roleCode}")
    @ProxyUser
    public ResponseEntity<Void> addAccessRole(@PathVariable("username") @ApiParam(value = "The username of the user.", required = true) final String username, @PathVariable("roleCode") @ApiParam(value = "access role code", required = true) final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode);
        return added ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "Roles have been added or user already had the role(s)"),
            @ApiResponse(code = 404, message = "The role(s) is not recognised or user cannot access caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles"),
    })
    @ApiOperation(value = "Add the given access roles to the user.", nickname = "addAccessRoles")
    @PostMapping("/{username}/access-role")
    @ProxyUser
    public void addAccessRoles(@PathVariable @ApiParam(value = "The username of the user.", required = true) final String username,
                               @RequestBody @NotEmpty final List<String> roles) {
        roles.forEach(r -> userService.addAccessRole(username, r));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "User already has role"),
            @ApiResponse(code = 201, message = "Role has been successfully added to user"),
            @ApiResponse(code = 404, message = "The role is not recognised or user cannot access caseload"),
            @ApiResponse(code = 403, message = "The current user doesn't have permission to maintain user roles")})
    @ApiOperation(value = "Add the given access role to the user and caseload.", notes = "Add the given access role to the user and caseload.", nickname = "addAccessRoleByCaseload")
    @PutMapping("/{username}/caseload/{caseload}/access-role/{roleCode}")
    @ProxyUser
    public ResponseEntity<Void> addAccessRoleByCaseload(@PathVariable("username") @ApiParam(value = "The username of the user.", required = true) final String username, @PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload, @PathVariable("roleCode") @ApiParam(value = "access role code", required = true) final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode, caseload);
        return added ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Current user detail.", notes = "Current user detail.", nickname = "getMyUserInformation")
    @GetMapping("/me")
    public UserDetail getMyUserInformation() {
        return userService.getUserByUsername(authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseLoad.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of caseloads accessible to current user.", notes = "List of caseloads accessible to current user.", nickname = "getMyCaseLoads")
    @GetMapping("/me/caseLoads")
    public List<CaseLoad> getMyCaseLoads(@RequestParam(value = "allCaseloads", required = false, defaultValue = "false") @ApiParam(value = "If set to true then all caseloads are returned", defaultValue = "false") final boolean allCaseloads) {
        return userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", notes = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", nickname = "getMyCaseNoteTypes")
    @GetMapping("/me/caseNoteTypes")
    public List<ReferenceCode> getMyCaseNoteTypes(@RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>code, activeFlag, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var currentCaseLoad =
                caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());

        final var caseLoadType = currentCaseLoad.isPresent() ? currentCaseLoad.get().getType() : "BOTH";
        return caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of locations accessible to current user.", notes = "List of locations accessible to current user.", nickname = "getMyLocations")
    @GetMapping("/me/locations")
    public List<Location> getMyLocations() {
        return locationService.getUserLocations(authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = UserRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of roles for current user.", notes = "List of roles for current user.", nickname = "getMyRoles")
    @GetMapping("/me/roles")
    public List<UserRole> getMyRoles(@RequestParam(value = "allRoles", required = false, defaultValue = "false") @ApiParam(value = "If set to true then all roles are returned rather than just API roles", defaultValue = "false") final boolean allRoles) {
        return userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);
    }

    @ApiResponses({
            @ApiResponse(code = 401, message = "Invalid username or password", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "the user does not have permission to view the caseload.", response = ErrorResponse.class)})
    @ApiOperation(value = "Update working caseload for current user.", notes = "Update working caseload for current user.", nickname = "updateMyActiveCaseLoad")
    @PutMapping("/me/activeCaseLoad")
    @HasWriteScope
    @ProxyUser
    public ResponseEntity<?> updateMyActiveCaseLoad(@RequestBody @ApiParam(value = "", required = true) final CaseLoad caseLoad) {
        try {
            userService.setActiveCaseLoad(authenticationFacade.getCurrentUsername(), caseLoad.getCaseLoadId());
        } catch (final AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .userMessage("Not Authorized")
                            .developerMessage("The current user does not have acess to this CaseLoad")
                            .build());
        }
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = UserDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "User detail.", notes = "User detail.", nickname = "getUserDetails")
    @GetMapping("/{username}")
    public UserDetail getUserDetails(@PathVariable("username") @ApiParam(value = "The username of the user.", required = true) final String username) {
        return userService.getUserByUsername(username.toUpperCase());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of user details", response = UserDetail.class, responseContainer = "List")})
    @ApiOperation(value = "Returns the user details for supplied usernames - POST version to allow large user lists.", notes = "user details for supplied usernames", nickname = "getUserDetailsList")
    @PostMapping("/list")
    public List<UserDetail> getUserDetailsList(@RequestBody @ApiParam(value = "The required usernames (mandatory)", required = true) final Set<String> usernames) {
        return userService.getUserListByUsernames(usernames);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = AccessRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of roles for the given user and caseload", notes = "List of roles for the given user and caseload", nickname = "getRolesForUserAndCaseload")
    @GetMapping("/{username}/access-roles/caseload/{caseload}")
    public List<AccessRole> getRolesForUserAndCaseload(@PathVariable("username") @ApiParam(value = "user account to filter by", required = true) final String username, @PathVariable("caseload") @ApiParam(value = "Caseload Id", required = true) final String caseload, @RequestParam(value = "includeAdmin", defaultValue = "false", required = false) @ApiParam(value = "Include admin roles", required = true, defaultValue = "false") final boolean includeAdmin) {
        return userService.getAccessRolesByUserAndCaseload(username, caseload, includeAdmin);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "No New Users", response = CaseloadUpdate.class),
            @ApiResponse(code = 201, message = "New Users Enabled", response = CaseloadUpdate.class),
    })
    @ApiOperation(value = "Add the NWEB caseload to specified caseload.", notes = "Add the NWEB caseload to specified caseload.", nickname = "addApiAccessForCaseload")
    @PutMapping("/add/default/{caseload}")
    @ProxyUser
    public ResponseEntity<CaseloadUpdate> addApiAccessForCaseload(@PathVariable("caseload") @ApiParam(value = "The caseload (equates to prison) id to add all active users to default API caseload (NWEB)", required = true) final String caseload) {
        final var caseloadUpdate = userService.addDefaultCaseloadForPrison(caseload);
        if (caseloadUpdate.getNumUsersEnabled() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(caseloadUpdate);
        }
        return ResponseEntity.ok().body(caseloadUpdate);
    }

}
