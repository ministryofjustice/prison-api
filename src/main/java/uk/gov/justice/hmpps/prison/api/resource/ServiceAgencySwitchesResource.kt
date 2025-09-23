package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.ServiceAgencySwitchesService

@RestController
@Tag(name = "agency-switches")
@RequestMapping(value = ["\${api.base.path}/agency-switches"], produces = ["application/json"])
class ServiceAgencySwitchesResource(private val service: ServiceAgencySwitchesService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Retrieve a list of agencies switched on for the service code",
    description = """Returns a list of agencies switched on for the service code.
      A special agencyId of `*ALL*` is used to designate that the service is switched on for all agencies.
      An agency in this context is normally a prison, but can also be any agency location e.g. prisoner escort service area.
      Requires ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO or ROLE_SERVICE_AGENCY_SWITCHES.
    """,
  )
  @PreAuthorize("hasAnyRole('SERVICE_AGENCY_SWITCHES', 'PRISON_API__SERVICE_AGENCY_SWITCHES__RO')")
  @GetMapping("/{serviceCode}")
  fun getServiceAgencies(
    @PathVariable
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
  ): List<AgencyDetails> = service.getServiceAgencies(serviceCode)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "Service is switched on for the service code and agency id."),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code does not exist or the service is not switched on for the prison.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Returns if the service is switched on for the specified service code / agency id.",
    description = """Returns 204 if the service is switched on for the service code / agency id combination.
    If the service is not switched on then 404 is returned.
    This endpoint also takes into account the special `*ALL*` agency id - if the service code has a agency entry of
    `*ALL*` then the service is deemed to be switched on for all agencies and will therefore return 204 irrespective of the
    agency id that is passed in.
    An agency in this context is normally a prison, but can also be any agency location e.g. prisoner escort service area.
    Requires ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO.
  """,
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SERVICE_AGENCY_SWITCHES__RO')")
  @GetMapping("/{serviceCode}/agency/{agencyId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun checkServiceAgency(
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table", example = "ACTIVITY") @PathVariable serviceCode: String,
    @Parameter(description = "The id of the agency", example = "MDI") @PathVariable agencyId: String,
  ) {
    if (!service.checkServiceSwitchedOnForAgency(serviceCode, agencyId)) {
      throw EntityNotFoundException("Service $serviceCode not turned on for agency $agencyId")
    }
  }

  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Created"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code or agency does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "409", description = "The agency is already active for the service", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Activates an agency for the given service", description = "Requires ROLE_SERVICE_AGENCY_SWITCHES")
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @PostMapping("/{serviceCode}/agency/{agencyId}")
  @ResponseStatus(HttpStatus.CREATED)
  fun addServiceAgency(
    @PathVariable
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
    @PathVariable
    @Parameter(description = "The id of the agency from the AGENCY_LOCATIONS table")
    agencyId: String,
  ): AgencyDetails = service.addServiceAgency(serviceCode, agencyId)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "OK"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code or agency does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Deactivates an agency for the given service", description = "Requires ROLE_SERVICE_AGENCY_SWITCHES")
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @DeleteMapping("/{serviceCode}/agency/{agencyId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeServiceAgency(
    @PathVariable
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
    @PathVariable
    @Parameter(description = "The id of the agency from the AGENCY_LOCATIONS table")
    agencyId: String,
  ) = service.removeServiceAgency(serviceCode, agencyId)
}

@Schema(description = "A agency")
data class AgencyDetails(
  @Schema(description = "The agency code. Normally a prison, but can be any location e.g. a prisoner escort service area.", example = "BXI")
  val agencyId: String,
  @Schema(description = "The agency name", example = "Brixton")
  val name: String,
)
