package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.service.PrisonerSearchService

@RestController
@Tag(name = "prisoner-search")
@RequestMapping(value = ["\${api.base.path}/prisoner-search"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('PRISONER_INDEX')")
class PrisonerSearchResource(private val service: PrisonerSearchService) {

  @GetMapping("/offenders/{offenderNo}")
  @Operation(
    summary = "Returns details required by Prisoner Search for the given offender number.",
    description = "This endpoint is dedicated to returning the details required by Prisoner Search so the role and endpoint are not for general use. If you're thinking of calling this endpoint try calling Prisoner Search instead.",
  )
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "OK",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = PrisonerSearchDetails::class))],
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
  fun getPrisonerDetails(
    @PathVariable("offenderNo") @Parameter(description = "offenderNo", example = "A1234AA") offenderNo: String,
  ): PrisonerSearchDetails = service.getPrisonerDetails(offenderNo)
}
