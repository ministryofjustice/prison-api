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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import uk.gov.justice.hmpps.prison.core.ProgrammaticAuthorisation;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.ReferenceData;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
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
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDetail.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Current user detail.", description = "Current user detail.")
    @ProgrammaticAuthorisation("Returns information about the current user only")
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
    @ProgrammaticAuthorisation("Returns information about the current user only")
    @GetMapping("/me/caseLoads")
    @SlowReportQuery
    public List<CaseLoad> getMyCaseLoads(@RequestParam(value = "allCaseloads", required = false, defaultValue = "false") @Parameter(description = "If set to true then all caseloads are returned") final boolean allCaseloads) {
        return userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", description = "List of all case note types (with sub-types) accessible to current user (and based on working caseload).", hidden = true)
    @ReferenceData(description = "Only case note types with sub-types are returned")
    @GetMapping("/me/caseNoteTypes") // Only called by DPS (hmpps-prisoner-profile)
    public List<ReferenceCode> getMyCaseNoteTypes() {
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
    @ProgrammaticAuthorisation("Returns information about the current user only")
    @GetMapping("/me/locations") // Called by a lot of auth clients
    @SlowReportQuery
    public List<Location> getMyLocations(
        @RequestParam(value = "include-non-residential-locations", required = false, defaultValue = "false") @Parameter(description = "Indicates non residential locations should be included") final boolean includeNonRes) {
        return locationService.getUserLocations(authenticationFacade.getCurrentUsername(), includeNonRes);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of roles for current user.", description = "List of roles for current user.")
    @ProgrammaticAuthorisation("Returns information about the current user only")
    @GetMapping("/me/roles")
    public List<UserRole> getMyRoles(@RequestParam(value = "allRoles", required = false, defaultValue = "false") @Parameter(description = "If set to true then all roles are returned rather than just API roles") final boolean allRoles) {
        return userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "the user does not have permission to view the caseload.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update working caseload for current user.", description = "Update working caseload for current user.")
    @PutMapping("/me/activeCaseLoad") // Called by manage-prison-visits-client (client_credentials), and licences-cloudplatform and prison-api (authorization_code)
    @PreAuthorize("hasRole('STAFF_RW')")
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
    @ProgrammaticAuthorisation("Returns information about the current user only")
    @GetMapping("/{username}")
    @SlowReportQuery
    public UserDetail getUserDetails(@PathVariable("username") @Parameter(description = "The username of the user.", required = true) final String username) {
        return userService.getUserByUsername(username.toUpperCase());
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "The list of user details")})
    @Operation(summary = "Returns the user details for supplied usernames - POST version to allow large user lists.", description = "user details for supplied usernames")
    @PreAuthorize("hasRole('STAFF_SEARCH')")
    @PostMapping("/list")// Called by 3 clients (client_credentials), and by categorisation-tool and prison-staff-hub (authorization_code)
    public List<UserDetail> getUserDetailsList(@RequestBody @Parameter(description = "The required usernames (mandatory)", required = true) final Set<String> usernames) {
        return userService.getUserListByUsernames(usernames);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "No New Users", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseloadUpdate.class))}),
            @ApiResponse(responseCode = "201", description = "New Users Enabled", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseloadUpdate.class))}),
    })
    @Operation(summary = "Add the NWEB caseload to specified caseload.", description = "Add the NWEB caseload to specified caseload.")
    @PutMapping("/add/default/{caseload}") // This is the only endpoint which is only called with client_credentials
    @PreAuthorize("hasRole('STAFF_RW')")
    @ProxyUser
    public ResponseEntity<CaseloadUpdate> addApiAccessForCaseload(@PathVariable("caseload") @Parameter(description = "The caseload (equates to prison) id to add all active users to default API caseload (NWEB)", required = true) final String caseload) {
        final var caseloadUpdate = userService.addDefaultCaseloadForPrison(caseload);
        if (caseloadUpdate.getNumUsersEnabled() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(caseloadUpdate);
        }
        return ResponseEntity.ok().body(caseloadUpdate);
    }
}
