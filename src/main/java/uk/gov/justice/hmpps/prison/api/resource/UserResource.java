package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.core.ProgrammaticAuthorisation;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.LocationService;
import uk.gov.justice.hmpps.prison.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@Tag(name = "users")
@Validated
@RequestMapping(value = "${api.base.path}/users", produces = "application/json")
@AllArgsConstructor
public class UserResource {
    private final HmppsAuthenticationHolder hmppsAuthenticationHolder;
    private final UserService userService;
    private final LocationService locationService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDetail.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Current user detail.", description = "Current user detail.")
    @ProgrammaticAuthorisation("Returns information about the current user only")
    @GetMapping("/me")
    public UserDetail getMyUserInformation() {
        return userService.getUserByUsername(hmppsAuthenticationHolder.getUsername());
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
        return userService.getCaseLoads(hmppsAuthenticationHolder.getUsername(), allCaseloads);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of locations accessible to current user.", description = "Deprecated - Use /locations/prison/{prisonId}/residential-first-level instead in the locations-inside-prison-api", deprecated = true)
    @ProgrammaticAuthorisation("Returns information about the current user only")
    @GetMapping("/me/locations")
    @SlowReportQuery
    @Deprecated(forRemoval = true)
    public List<Location> getMyLocations(
        @RequestParam(value = "include-non-residential-locations", required = false, defaultValue = "false") @Parameter(description = "Indicates non residential locations should be included") final boolean includeNonRes) {
        return locationService.getUserLocations(hmppsAuthenticationHolder.getUsername(), includeNonRes);
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
        return userService.getRolesByUsername(hmppsAuthenticationHolder.getUsername(), allRoles);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "the user does not have permission to view the caseload.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update working caseload for current user.", description = "Update working caseload for current user.")
    @PutMapping("/me/activeCaseLoad")
    @ProgrammaticAuthorisation("Access is checked in the service")
    @ProxyUser
    public void updateMyActiveCaseLoad(@RequestBody @Parameter(required = true) final CaseLoad caseLoad) {
        userService.setActiveCaseLoad(hmppsAuthenticationHolder.getUsername(), caseLoad.getCaseLoadId());
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
    @Operation(summary = "Returns the user details for supplied usernames - POST version to allow large user lists.", description = "Requires role STAFF_SEARCH")
    @PreAuthorize("hasRole('STAFF_SEARCH')")
    @PostMapping("/list")
    public List<UserDetail> getUserDetailsList(@RequestBody @Parameter(description = "The required usernames (mandatory)", required = true) final Set<String> usernames) {
        return userService.getUserListByUsernames(usernames);
    }
}
