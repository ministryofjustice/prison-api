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
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.ServiceAgencySwitchesService

@RestController
@Tag(name = "agency-switches")
@Deprecated("Use /agency-switches instead")
@RequestMapping(value = ["\${api.base.path}/service-prisons"], produces = ["application/json"])
class ServicePrisonsResource(private val service: ServiceAgencySwitchesService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Retrieve a list of prisons switched on for the service code",
    description = """Returns a list of prisons switched on for the service code.
      A special prisonId of `*ALL*` is used to designate that the service is switched on for all prisons.
      Deprecated - use /agency-switches/{serviceCode} instead.
      Requires ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO.
    """,
  )
  @PreAuthorize("hasAnyRole('SERVICE_AGENCY_SWITCHES', 'PRISON_API__SERVICE_AGENCY_SWITCHES__RO')")
  @GetMapping("/{serviceCode}")
  fun getServicePrisons(
    @PathVariable
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
  ): List<PrisonDetails> = service.getServicePrisons(serviceCode)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "Service is switched on for the service code and prison id."),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code does not exist or the service is not switched on for the prison.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Returns if the service is switched on for the specified service code / prison id.",
    description = """Returns 204 if the service is switched on for the service code / prison id combination.
    If the service is not switched on then 404 is returned.
    This endpoint also takes into account the special `*ALL*` prison id - if the service code has a prison entry of
    `*ALL*` then the service is deemed to be switched on for all prisons and will therefore return 204 irrespective of the
    prison id that is passed in.
    Deprecated - use /agency-switches/{serviceCode}/agency/{agencyId} instead.
    Requires ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO.
  """,
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SERVICE_AGENCY_SWITCHES__RO')")
  @GetMapping("/{serviceCode}/prison/{prisonId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun checkServicePrison(
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table", example = "ACTIVITY") @PathVariable serviceCode: String,
    @Parameter(description = "The id of the prison", example = "MDI") @PathVariable prisonId: String,
  ) {
    if (!service.checkServiceSwitchedOnForAgency(serviceCode, prisonId)) {
      throw EntityNotFoundException("Service $serviceCode not turned on for prison $prisonId")
    }
  }

  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Created"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code or prison does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "409", description = "The prison is already active for the service", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Activates a prison for the given service",
    description = "Deprecated - use /agency-switches/{serviceCode}/agency/{agencyId} instead.",
  )
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @PostMapping("/{serviceCode}/prison/{prisonId}")
  @ResponseStatus(HttpStatus.CREATED)
  fun addServicePrison(
    @PathVariable
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
    @PathVariable
    @Parameter(description = "The id of the prison from the AGENCY_LOCATIONS table")
    prisonId: String,
  ): PrisonDetails = service.addServicePrison(serviceCode, prisonId)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "OK"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code or prison does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Deactivates a prison for the given service",
    description = "Deprecated - use /agency-switches/{serviceCode}/agency/{agencyId} instead.",
  )
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @DeleteMapping("/{serviceCode}/prison/{prisonId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeServicePrison(
    @PathVariable
    @Parameter(description = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
    @PathVariable
    @Parameter(description = "The id of the prison from the AGENCY_LOCATIONS table")
    prisonId: String,
  ) = service.removeServiceAgency(serviceCode, prisonId)
}
