package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction
import uk.gov.justice.hmpps.prison.api.model.TransferTransactionDetail
import uk.gov.justice.hmpps.prison.service.FinanceService
import uk.gov.justice.hmpps.prison.util.ResourceUtils

@RestController
@Tag(name = "finance")
@RequestMapping(value = ["\${api.base.path}/finance"], produces = ["application/json"])
@PreAuthorize("hasAnyRole('NOMIS_API_V1', 'UNILINK', 'PRISON_API__HMPPS_INTEGRATION_API')")
@Validated
class FinanceController(
  private val financeService: FinanceService,
) {
  @PostMapping("/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings")
  @Operation(
    summary = "Post a financial transaction to NOMIS.",
    description = """
            Notes:<br/>
              <ul>
                <li>If the field X-Client-Name is present in the request header then the value is prepended to the client_unique_ref separated by a dash</li>
                <li>The client_unique_ref can have a maximum of 64 characters, only alphabetic, numeric, ‘-’ and ‘_’ characters are allowed</li>
              </ul>
            <p>Requires NOMIS_API_V1, UNILINK or PRISON_API__HMPPS_INTEGRATION_API role.</p>
            """,
  )
  @Tag(name = "integration-api")
  @Tag(name = "unilink")
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "Transaction Created"),
    ApiResponse(
      responseCode = "400",
      description = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
        "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
        "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "409",
      description = "Duplicate post - The unique_client_ref has been used before",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  fun transferToSavings(
    @Parameter(name = "X-Client-Name", description = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.")
    @RequestHeader(value = "X-Client-Name", required = false)
    clientName: String?,
    @Parameter(description = "Prison ID", example = "BMI", required = true)
    @PathVariable
    @Size(max = 3)
    prisonId: String,
    @Parameter(description = "Offender Noms Id", example = "A1417AE", required = true)
    @Pattern(regexp = NomisApiV1Resource.NOMS_ID_REGEX_PATTERN)
    @PathVariable
    offenderNo: String,
    @Parameter(description = "Saving Transfer Transaction Details", required = true)
    @RequestBody
    @Valid
    transferTransaction: TransferTransaction,
  ): TransferTransactionDetail {
    val uniqueClientId = ResourceUtils.getUniqueClientId(clientName, transferTransaction.clientUniqueRef)

    return financeService.transferToSavings(prisonId, offenderNo, transferTransaction, uniqueClientId)
  }
}
