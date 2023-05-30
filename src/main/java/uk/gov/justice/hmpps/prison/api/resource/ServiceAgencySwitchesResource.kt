package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Hidden
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
import uk.gov.justice.hmpps.prison.service.ServiceAgencySwitchesService

@RestController
@Tag(name = "service-prisons")
@RequestMapping(value = ["\${api.base.path}/service-prisons"], produces = ["application/json"])
class ServiceAgencySwitchesResource(private val service: ServiceAgencySwitchesService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Retrieve a list of prisons switched on for the service code")
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @GetMapping("/{serviceCode}")
  @Hidden // TODO SDIT-797 Remove annotation once tables EXTERNAL_SERVICES  and SERVICE_AGENCY_SWITCHES are live
  fun getServicePrisons(
    @PathVariable
    @Parameter(name = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
  ): List<PrisonDetails> =
    service.getServicePrisons(serviceCode)

  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Created"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code or prison does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "409", description = "The prison is already active for the service", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Activates a prison for the given service")
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @PostMapping("/{serviceCode}/prison/{prisonId}")
  @ResponseStatus(HttpStatus.CREATED)
  @Hidden // TODO SDIT-797 Remove annotation once tables EXTERNAL_SERVICES  and SERVICE_AGENCY_SWITCHES are live
  fun addServicePrison(
    @PathVariable
    @Parameter(name = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
    @PathVariable
    @Parameter(name = "The id of the prison from the AGENCY_LOCATIONS table")
    prisonId: String,
  ): PrisonDetails =
    service.addServicePrison(serviceCode, prisonId)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "OK"),
    ApiResponse(responseCode = "401", description = "A valid auth token was not presented", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "The auth token does not have the necessary role", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "The service code or prison does not exist", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Deactivates a prison for the given service")
  @PreAuthorize("hasRole('SERVICE_AGENCY_SWITCHES')")
  @DeleteMapping("/{serviceCode}/prison/{prisonId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Hidden // TODO SDIT-797 Remove annotation once tables EXTERNAL_SERVICES  and SERVICE_AGENCY_SWITCHES are live
  fun removeServicePrison(
    @PathVariable
    @Parameter(name = "The code of the service from the EXTERNAL_SERVICES table")
    serviceCode: String,
    @PathVariable
    @Parameter(name = "The id of the prison from the AGENCY_LOCATIONS table")
    prisonId: String,
  ) =
    service.removeServicePrison(serviceCode, prisonId)
}
