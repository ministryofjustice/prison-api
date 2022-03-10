package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

import java.util.List;
import java.util.Set;

@RestController
@Tag(name = "users")
@Validated
@RequestMapping(value = "${api.base.path}/users", produces = "application/json")
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
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get staff details for local administrator", description = "Get user details for local administrator")
    @GetMapping("/local-administrator/available")
    public ResponseEntity<List<UserDetail>> getStaffUsersForLocalAdministrator(@RequestParam(value = "nameFilter", required = false) @Parameter(description = "Filter results by first name and/or username and/or last name of staff member.") final String nameFilter,
                                                                               @RequestParam(value = "accessRole", required = false) @Parameter(description = "Filter results by access role") final List<String> accessRoles,
                                                                               @RequestParam(value = "status", required = false, defaultValue = "ALL") @Parameter(description = "Limit to active / inactive / show all users.") final Status status,
                                                                               @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of caseload records.") final Long pageOffset,
                                                                               @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of caseload records returned.") final Long pageLimit,
                                                                               @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields,
                                                                               @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsersAsLocalAdministrator(authenticationFacade.getCurrentUsername(), nameFilter, accessRoles, status, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get user details.", description = "Get user details.")
    @GetMapping
    public ResponseEntity<List<UserDetail>> getUsers(@RequestParam(value = "nameFilter", required = false) @Parameter(description = "Filter results by first name and/or username and/or last name of staff member.") final String nameFilter,
                                                     @RequestParam(value = "accessRole", required = false) @Parameter(description = "Filter results by access roles") final List<String> accessRoles,
                                                     @RequestParam(value = "status", required = false, defaultValue = "ALL") @Parameter(description = "Limit to active / inactive / show all users.") final Status status,
                                                     @RequestParam(value = "caseload", required = false) @Parameter(description = "Filter results to include only those users that have access to the specified caseload (irrespective of whether it is currently active or not", example = "MDI") final String caseload,
                                                     @RequestParam(value = "activeCaseload", required = false) @Parameter(description = "Filter results by user's currently active caseload i.e. the one they have currently selected.", example = "MDI") final String activeCaseload,
                                                     @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false)
                                                         @Parameter(description = "Requested offset of first record in returned collection of user records.") final Long pageOffset,
                                                     @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false)
                                                         @Parameter(description = "Requested limit to number of user records returned.") final Long pageLimit,
                                                     @RequestHeader(value = "Sort-Fields", required = false)
                                                         @Parameter(description = "Comma separated list of one or more of the following fields - <b>firstName, lastName</b>") final String sortFields,
                                                     @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false)
                                                         @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsers(nameFilter, accessRoles, status,
            StringUtils.trimToNull(caseload), StringUtils.trimToNull(activeCaseload), pageRequest);

        return ResponseEntity.ok()
            .headers(userDetails.getPaginationHeaders())
            .body(userDetails.getItems());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDetail.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Current user detail.", description = "Current user detail.")
    @GetMapping("/me")
    public UserDetail getMyUserInformation() {
        return userService.getUserByUsername(authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of caseloads accessible to current user.", description = "List of caseloads accessible to current user.")
    @GetMapping("/me/caseLoads")
    public List<CaseLoad> getMyCaseLoads(@RequestParam(value = "allCaseloads", required = false, defaultValue = "false") @Parameter(description = "If set to true then all caseloads are returned") final boolean allCaseloads) {
        return userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", description = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).")
    @GetMapping("/me/caseNoteTypes")
    public List<ReferenceCode> getMyCaseNoteTypes(@RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>code, activeFlag, description</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var currentCaseLoad =
                caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());

        final var caseLoadType = currentCaseLoad.isPresent() ? currentCaseLoad.get().getType() : "BOTH";
        return caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of locations accessible to current user.", description = "List of locations accessible to current user.")
    @GetMapping("/me/locations")
    public List<Location> getMyLocations() {
        return locationService.getUserLocations(authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of roles for current user.", description = "List of roles for current user.")
    @GetMapping("/me/roles")
    public List<UserRole> getMyRoles(@RequestParam(value = "allRoles", required = false, defaultValue = "false") @Parameter(description = "If set to true then all roles are returned rather than just API roles") final boolean allRoles) {
        return userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "the user does not have permission to view the caseload.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update working caseload for current user.", description = "Update working caseload for current user.")
    @PutMapping("/me/activeCaseLoad")
    @HasWriteScope
    @ProxyUser
    public ResponseEntity<?> updateMyActiveCaseLoad(@RequestBody @Parameter(required = true) final CaseLoad caseLoad) {
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
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDetail.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "User detail.", description = "User detail.")
    @GetMapping("/{username}")
    public UserDetail getUserDetails(@PathVariable("username") @Parameter(description = "The username of the user.", required = true) final String username) {
        return userService.getUserByUsername(username.toUpperCase());
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "The list of user details")})
    @Operation(summary = "Returns the user details for supplied usernames - POST version to allow large user lists.", description = "user details for supplied usernames")
    @PostMapping("/list")
    public List<UserDetail> getUserDetailsList(@RequestBody @Parameter(description = "The required usernames (mandatory)", required = true) final Set<String> usernames) {
        return userService.getUserListByUsernames(usernames);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "No New Users", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseloadUpdate.class))}),
            @ApiResponse(responseCode = "201", description = "New Users Enabled", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseloadUpdate.class))}),
    })
    @Operation(summary = "Add the NWEB caseload to specified caseload.", description = "Add the NWEB caseload to specified caseload.")
    @PutMapping("/add/default/{caseload}")
    @ProxyUser
    public ResponseEntity<CaseloadUpdate> addApiAccessForCaseload(@PathVariable("caseload") @Parameter(description = "The caseload (equates to prison) id to add all active users to default API caseload (NWEB)", required = true) final String caseload) {
        final var caseloadUpdate = userService.addDefaultCaseloadForPrison(caseload);
        if (caseloadUpdate.getNumUsersEnabled() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(caseloadUpdate);
        }
        return ResponseEntity.ok().body(caseloadUpdate);
    }

}
