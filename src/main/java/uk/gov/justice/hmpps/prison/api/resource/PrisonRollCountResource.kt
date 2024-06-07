package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.core.SlowReportQuery
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess
import uk.gov.justice.hmpps.prison.service.PrisonRollCount
import uk.gov.justice.hmpps.prison.service.PrisonRollCountService

@RestController
@Tag(name = "prison")
@Validated
@RequestMapping(value = ["/api/prison/roll-count/{prisonId}"], produces = ["application/json"])
class PrisonRollCountResource(
  private val prisonRollCountService: PrisonRollCountService,
) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Roll count for a specific prison",
    description = "Requires role ESTABLISHMENT_ROLL or agency in caseload.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns cell list of roll-counts for a specific prison and sub-location",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires the ESTABLISHMENT_ROLL role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @SlowReportQuery
  @VerifyAgencyAccess(overrideRoles = ["ESTABLISHMENT_ROLL"])
  fun getPrisonRollCount(
    @Schema(description = "Prison Id", example = "MDI", required = true, minLength = 3, maxLength = 5, pattern = "^[A-Z]{2}I|ZZGHI$")
    @PathVariable
    prisonId: String,
    @RequestParam(name = "include-cells", required = false, defaultValue = "false") includeCells: Boolean = false,
  ): PrisonRollCount =
    prisonRollCountService.getPrisonRollCount(prisonId, includeCells)

  @GetMapping("/cells-only/{locationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Provides the list of cells with roll counts under a specified location provided",
    description = "Requires role ESTABLISHMENT_ROLL or agency in caseload.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns establishment roll count for a prison",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing required role. Requires the ESTABLISHMENT_ROLL role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @SlowReportQuery
  @VerifyAgencyAccess(overrideRoles = ["ESTABLISHMENT_ROLL"])
  fun getCellLevelRollCount(
    @Schema(description = "Prison Id", example = "MDI", required = true, minLength = 3, maxLength = 5, pattern = "^[A-Z]{2}I|ZZGHI$")
    @PathVariable
    prisonId: String,
    @Schema(description = "Location ID of parent of the cells", required = true, example = "1212312")
    @PathVariable
    locationId: String,
  ): PrisonRollCount =
    prisonRollCountService.getPrisonCellRollCount(prisonId, locationId)
}
