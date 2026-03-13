package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.TransactionsService
import kotlin.String

@RestController
@Tag(name = "TODO")
@Validated
@RequestMapping(value = [$$"${api.base.path}/prison/{prisonId}/offenders/{nomsId}/finance/holds"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('NOMIS_API_V1')")
class FinanceHoldsResource(private val service: TransactionsService) {
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add money from a trust account on hold",
    responses = [
      ApiResponse(responseCode = "201", description = "Hold added"),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun addHold(
    @PathVariable("prisonId")
    @Parameter(description = "Prison ID", example = "MDI", required = true)
    prisonId: String,
    @PathVariable("nomsId")
    @Parameter(description = "Prisoner unique reference", example = "A1234AA", required = true)
    nomisId: String,
    @RequestBody request: AddHoldRequest,
  ): FinanceHold = service.addHold(prisonId, nomisId, request)

  @DeleteMapping("/{holdNumber}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeHold(
    @PathVariable("prisonId")
    @Parameter(description = "Prison ID", example = "MDI", required = true)
    prisonId: String,
    @PathVariable("nomsId")
    @Parameter(description = "Prisoner unique reference", example = "A1234AA", required = true)
    nomisId: String,
    @PathVariable
    @Parameter(description = "Remove a hold from an account")
    holdNumber: Long,
  ) = service.removeHold(prisonId, nomisId, holdNumber)
}

@Schema(description = "Hold details to be added against a prisoner's account.")
data class AddHoldRequest(
  @Schema(description = "The sub account type to assign the hold against", example = "spends", required = true)
  val subAccountType: String,
  @Schema(description = "Amount to put on hold in pence.", example = "1230", required = true)
  val amount: Long,
  @Schema(description = "Reason for the hold", example = "Canteen hold")
  val holdDescription: String,
)

data class FinanceHold(
  val holdNumber: Long,
)
