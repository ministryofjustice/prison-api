package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
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
import uk.gov.justice.hmpps.prison.service.PrisonRollSummary

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
        description = "Returns roll count for a specific prison",
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

  @GetMapping("/summary")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Roll count summary for a specific prison",
    description = "Requires role ESTABLISHMENT_ROLL or agency in caseload.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns roll count summary for a specific prison",
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
  fun getPrisonRollCountSummary(
    @Schema(description = "Prison Id", example = "MDI", required = true, minLength = 3, maxLength = 5, pattern = "^[A-Z]{2}I|ZZGHI$")
    @PathVariable
    prisonId: String,
  ): PrisonRollSummary =
    prisonRollCountService.getPrisonRollSummary(prisonId)

  @GetMapping("/cells-only/{locationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Provides the list of cells with roll counts under a specified location provided",
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
  fun getCellLevelRollCount(
    @Schema(description = "Prison Id", example = "MDI", required = true, minLength = 3, maxLength = 5, pattern = "^[A-Z]{2}I|ZZGHI$")
    @PathVariable
    prisonId: String,
    @Schema(description = "Location ID of parent of the cells", required = true, example = "1212312")
    @PathVariable
    locationId: String,
  ): PrisonRollCount =
    prisonRollCountService.getPrisonCellRollCount(prisonId, locationId)

  @GetMapping("/movement-count")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Provide the IN/OUT movements and en-route counts for today",
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
  @VerifyAgencyAccess(overrideRoles = ["ESTABLISHMENT_ROLL"])
  fun getRollCountMovementInformation(
    @Schema(description = "Prison Id", example = "MDI", required = true, minLength = 3, maxLength = 5, pattern = "^[A-Z]{2}I|ZZGHI$")
    @Size(min = 3, message = "Prison ID must be a minimum of 3 characters")
    @NotBlank(message = "Prison ID cannot be blank")
    @Size(max = 5, message = "Prison ID cannot be more than 5 characters")
    @Pattern(regexp = "^[A-Z]{2}I|ZZGHI$", message = "Prison ID must be 3 characters ending in an I or ZZGHI")
    @PathVariable
    prisonId: String,
  ) = prisonRollCountService.getRollCountMovementInformation(prisonId)
}
