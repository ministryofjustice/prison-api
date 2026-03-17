package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.api.model.CaseLoad
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.api.model.UserDetail
import uk.gov.justice.hmpps.prison.api.model.UserRole
import uk.gov.justice.hmpps.prison.core.ProgrammaticAuthorisation
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.core.SlowReportQuery
import uk.gov.justice.hmpps.prison.service.LocationService
import uk.gov.justice.hmpps.prison.service.UserService
import java.util.*

@RestController
@Tag(name = "users")
@Validated
@RequestMapping(value = [$$"${api.base.path}/users"], produces = ["application/json"])
class UserResource(
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder,
  private val userService: UserService,
  private val locationService: LocationService,
) {
  @GetMapping("/me")
  @Operation(summary = "Current user detail.", description = "Current user detail.")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "OK",
    ),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ErrorResponse::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ErrorResponse::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ErrorResponse::class),
        ),
      ],
    ),
  )
  fun getMyUserInformation():
    @ProgrammaticAuthorisation(value = "Returns information about the current user only")
    UserDetail = userService.getUserByUsername(hmppsAuthenticationHolder.username)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "List of caseloads accessible to current user.",
    description = "List of caseloads accessible to current user.",
  )
  @GetMapping("/me/caseLoads")
  @SlowReportQuery
  fun getMyCaseLoads(
    @RequestParam(value = "allCaseloads", required = false, defaultValue = "false")
    @Parameter(description = "If set to true then all caseloads are returned") allCaseloads: Boolean,
  ):
    @ProgrammaticAuthorisation("Returns information about the current user only")
    List<CaseLoad> = userService.getCaseLoads(hmppsAuthenticationHolder.username, allCaseloads)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "List of locations accessible to current user.",
    description = "Deprecated - Use /locations/prison/{prisonId}/residential-first-level instead in the locations-inside-prison-api",
    deprecated = true,
  )
  @GetMapping("/me/locations")
  @SlowReportQuery
  @Deprecated("")
  fun getMyLocations(
    @RequestParam(value = "include-non-residential-locations", required = false, defaultValue = "false")
    @Parameter(description = "Indicates non residential locations should be included") includeNonRes: Boolean,
  ):
    @ProgrammaticAuthorisation("Returns information about the current user only")
    MutableList<Location> = locationService.getUserLocations(hmppsAuthenticationHolder.username, includeNonRes)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "List of roles for current user.", description = "List of roles for current user.")
  @GetMapping("/me/roles")
  fun getMyRoles(
    @RequestParam(value = "allRoles", required = false, defaultValue = "false")
    @Parameter(description = "If set to true then all roles are returned rather than just API roles") allRoles: Boolean,
  ):
    @ProgrammaticAuthorisation("Returns information about the current user only")
    List<UserRole> = userService.getRolesByUsername(hmppsAuthenticationHolder.username, allRoles)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "401",
      description = "Invalid username or password",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "the user does not have permission to view the caseload.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Update working caseload for current user.",
    description = "Update working caseload for current user.",
  )
  @PutMapping("/me/activeCaseLoad")
  @ProxyUser
  fun updateMyActiveCaseLoad(@RequestBody @Parameter(required = true) caseLoad: ActiveCaseLoad) {
    userService.setActiveCaseLoad(hmppsAuthenticationHolder.username, caseLoad.caseLoadId)
  }

  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "OK",
    ),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "User detail.", description = "User detail.")
  @GetMapping("/{username}")
  @SlowReportQuery
  fun getUserDetails(
    @PathVariable("username") @Parameter(description = "The username of the user.", required = true) username: String,
  ):
    @ProgrammaticAuthorisation("Returns information about the current user only")
    UserDetail = userService.getUserByUsername(username.uppercase(Locale.getDefault()))

  @ApiResponses(ApiResponse(responseCode = "200", description = "The list of user details"))
  @Operation(
    summary = "Returns the user details for supplied usernames - POST version to allow large user lists.",
    description = "Requires role STAFF_SEARCH",
  )
  @PreAuthorize("hasRole('STAFF_SEARCH')")
  @PostMapping("/list")
  fun getUserDetailsList(
    @RequestBody @Parameter(description = "The required usernames (mandatory)", required = true) usernames: Set<String>,
  ): List<UserDetail> = userService.getUserListByUsernames(usernames)
}

data class ActiveCaseLoad(
  @Schema(requiredMode = REQUIRED, description = "Case Load ID", example = "MDI")
  val caseLoadId: String,
)
