package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.extern.slf4j.Slf4j
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateChargeAndOutcomes
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateResult
import uk.gov.justice.hmpps.prison.core.SlowReportQuery
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.service.courtdates.CourtDateService

@Slf4j
@RestController
@Tag(name = "court-dates")
@Validated
@RequestMapping(
  value = ["\${api.base.path}/court-date-results"],
  produces = ["application/json"],
)
class CourtDateResource(private val courtDateService: CourtDateService) {

  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The court date results.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CourtDateResult::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "Returns details of all court dates and the result of each.")
  @GetMapping("/{offenderNo}")
  @VerifyOffenderAccess(overrideRoles = ["GLOBAL_SEARCH", "VIEW_PRISONER_DATA"])
  @SlowReportQuery
  fun getCourtDateResults(
    @PathVariable("offenderNo") @Parameter(
      description = "The required offender id (mandatory)",
      required = true,
    ) offenderNo: String,
  ): List<CourtDateResult> = courtDateService.getCourtDateResultsFlat(offenderNo)

  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The court date results.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CourtDateChargeAndOutcomes::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "Returns details of all the court outcomes grouped by charge")
  @GetMapping("/by-charge/{offenderNo}")
  @VerifyOffenderAccess(overrideRoles = ["GLOBAL_SEARCH", "VIEW_PRISONER_DATA"])
  @SlowReportQuery
  fun getCourtDateChargesAndOutcomes(
    @PathVariable("offenderNo") @Parameter(
      description = "The required offender id (mandatory)",
      required = true,
    ) offenderNo: String,
  ): List<CourtDateChargeAndOutcomes> = courtDateService.getCourtDateResults(offenderNo)
}
