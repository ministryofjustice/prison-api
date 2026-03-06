package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.PrisonerRepairService

@RestController
@Hidden
@RequestMapping(value = ["\${api.base.path}/prisoner-repair"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('PRISON_API__PRISONER_REPAIR__RW')")
class PrisonerRepairResource(private val service: PrisonerRepairService) {

  @PostMapping("/{bookingId}/restricted-patient-movements")
  @Operation(
    summary = "Repair movements for a prisoner as a result of a restricted patient release.",
    description = """There was a bug in restricted patients in that when the patient was then released from hospital
      we didn't mark the movement to hospital as inactive. This endpoint fixes that issue.
      """,
  )
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "OK",
    ),
    ApiResponse(
      responseCode = "401",
      description = "Unauthorized to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Requires role ROLE_PRISONER_INDEX",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @ProxyUser
  fun updateMovements(
    @PathVariable("bookingId") @Parameter(description = "bookingId", example = "12345") bookingId: Long,
  ) {
    service.updateMovementsForRestrictedPatients(bookingId)
  }
}
