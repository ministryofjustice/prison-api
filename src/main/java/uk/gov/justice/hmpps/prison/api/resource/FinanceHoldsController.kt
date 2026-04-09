package uk.gov.justice.hmpps.prison.api.resource

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.FinanceHoldsService

@RestController
@Tag(name = "canteen-funds-api")
@PreAuthorize("hasRole('PRISON_API__CANTEEN_FUNDS_API__RW')")
@RequestMapping(value = [$$"${api.base.path}/finance-holds"], produces = ["application/json"])
@Validated
class FinanceHoldsController(
  private val financeHoldsService: FinanceHoldsService,
) {
  companion object {
    const val NOMS_ID_REGEX_PATTERN: String = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}"
  }

  @PostMapping("/prison/{prisonId}/offenders/{offenderNo}/add-hold")
  @ProxyUser
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add a hold financial transaction to NOMIS.",
    description = """
      Add a financial hold (HOA transaction) to an offender’s account, reserving funds so that a future canteen transaction can be completed successfully.
      
      The valid prisonId and type combinations are defined in the Nomis transaction_operations table which is maintained by the
      Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. 
      Only those prisons (Caseloads) with HOA Transaction type associated with the NOMISAPI module are valid for an 'Add Hold' transaction.

      Used by the CMS replacement team to support canteen ordering.
      Requires PRISON_API__CANTEEN_FUNDS_API__RW role.
      """,
  )
  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Hold Added"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid Request",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "409",
      description = "Duplicate post - The clientUniqueReference has been used before",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  fun addHold(
    @Parameter(description = "Prison ID", example = "BMI", required = true)
    @PathVariable
    @Size(max = 3)
    prisonId: String,
    @Parameter(description = "Offender Noms Id", example = "A1417AE", required = true)
    @Pattern(regexp = NOMS_ID_REGEX_PATTERN)
    @PathVariable
    offenderNo: String,
    @Parameter(description = "Hold Transaction Details", required = true)
    @RequestBody
    @Valid
    holdTransaction: AddHoldTransaction,
  ): HoldDetails = financeHoldsService.addHold(prisonId, offenderNo, holdTransaction)

  @PostMapping("/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}")
  @ProxyUser
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Remove a hold from an existing hold financial transaction to NOMIS.",
    description = """
      Removes a financial hold (by adding a HOR transaction) on an offender’s account, making funds available so that a canteen transaction can be completed successfully.
      
      The valid prisonId and type combinations are defined in the Nomis transaction_operations table which is maintained by the
      Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. 
      Only those prisons (Caseloads) with HOR Transaction type associated with the NOMISAPI module are valid for a 'Remove Hold' transaction.

      Used by the CMS replacement team to support canteen ordering.
      Requires PRISON_API__CANTEEN_FUNDS_API__RW role.
      """,
  )
  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Hold removal transaction created"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid Request",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "409",
      description = "Duplicate post - The clientUniqueReference has been used before",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  fun releaseHold(
    @Parameter(description = "Prison ID", example = "BMI", required = true)
    @PathVariable
    @Size(max = 3)
    prisonId: String,
    @Parameter(description = "Offender Noms Id", example = "A1417AE", required = true)
    @Pattern(regexp = NOMS_ID_REGEX_PATTERN)
    @PathVariable
    offenderNo: String,
    @Parameter(description = "Hold Number", required = true)
    @PathVariable
    holdNumber: Long,
    @Parameter(description = "Release Hold Transaction Details", required = true)
    @RequestBody
    @Valid
    holdTransaction: ReleaseHoldTransaction,
  ) {
    financeHoldsService.releaseHold(prisonId, offenderNo, holdTransaction, holdNumber)
  }

  @ApiResponses(
    ApiResponse(
      responseCode = "201",
      description = "Transaction Created",
    ),
    ApiResponse(
      responseCode = "400",
      description = """Invalid request. For the create transaction one of: 
        <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>
        <li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>
        <li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>
        <li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>""",
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
  @Operation(
    summary = "Release the hold and post a financial transaction to NOMIS.",
    description = """This endpoint is the combination of two endpoints - it releases the hold and then creates the finance transaction.
      
            The valid prisonId and type combinations are defined in the Nomis transaction_operations table which is maintained by the
            Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. 
            Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid.
             
            This will be setup by script initially as part of the deployment process as shown below:
            
            | Transaction Type | Description                          | Digital Prison | Non Digital Prison |
            |------------------|--------------------------------------|----------------|--------------------|
            | CANT             | Canteen Spend                        | Yes            | No                 |
            | REFND            | Canteen Refund                       | Yes            | No                 |
            | PHONE            | Phone Credit                         | Yes            | No                 |
            | MRPR             | Misc Receipt - Private Cash          | Yes            | Yes                |
            | MTDS             | Money through digital service        | Yes            | Yes                |
            | DTDS             | Disbursement through Digital service | Yes            | Yes                |
            | CASHD            | Cash Disbursement                    | Yes            | Yes                |
            | RELA             | Money to Relatives                   | Yes            | Yes                |
            | RELS             | Money to Relatives- Spends           | Yes            | Yes                |

            Notes:
            - The sub_account the amount is debited or credited from will be determined by the transaction_type definition in NOMIS.
            - The clientUniqueReference can have a maximum of 64 characters, only alphabetic, numeric, ‘-’ and ‘_’ characters are allowed

            Requires PRISON_API__CANTEEN_FUNDS_API__RW role.""",
  )
  @PostMapping("/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  @ProxyUser
  fun releaseHoldAndCreateTransaction(
    @Parameter(description = "Prison ID", example = "BMI", required = true)
    @PathVariable
    @Size(max = 3)
    prisonId: String,
    @Parameter(description = "Offender Noms Id", example = "A1417AE", required = true)
    @Pattern(regexp = NOMS_ID_REGEX_PATTERN)
    @PathVariable
    offenderNo: String,
    @Parameter(description = "Hold Number", required = true)
    @PathVariable
    holdNumber: Long,
    @Parameter(description = "Release Hold and Create Transaction Details", required = true)
    @RequestBody
    @Valid
    createTransaction: ReleaseHoldAndCreateTransaction,
  ): Transaction {
    val result = financeHoldsService.releaseHoldAndCreateTransaction(prisonId, offenderNo, createTransaction, holdNumber)
    return Transaction(result)
  }
}

@Schema(description = "Hold Transaction to Create")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddHoldTransaction(
  @Schema(description = "Description of the Transaction", example = "Hold for Food", defaultValue = "HOLD")
  @Size(min = 1, max = 240, message = "The description must be between 1 and 240 characters")
  val description: String = "HOLD",

  @Schema(description = "Amount of transaction in pence, hence 1634 is £16.34", example = "1634", required = true)
  @Min(value = 1, message = "The amount must be greater than 0")
  val amount: Long,

  @Schema(description = "Client Transaction Id", example = "CL123212", required = true)
  @Size(min = 1, max = 12, message = "The client transaction ID must be between 1 and 12 characters")
  val clientTransactionId: String,

  @Schema(description = "Client Name", example = "CL123212")
  val clientName: String?,

  @Schema(
    description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed",
    example = "CLIENT121131-0_11",
  )
  @Size(min = 1, max = 64, message = "The client unique reference must be between 1 and 64 characters")
  @Pattern(
    regexp = "[a-zA-Z0-9-_]+",
    message = "The client unique reference can only contain letters, numbers, hyphens and underscores",
  )
  val clientUniqueReference: String,
)

@Schema(description = "Hold Transaction to Release")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReleaseHoldTransaction(
  @Schema(description = "Description of the Transaction", example = "Hold for Food", defaultValue = "Remove Hold")
  @Size(min = 1, max = 240, message = "The description must be between 1 and 240 characters")
  val description: String = "Remove Hold",

  @Schema(description = "Client Transaction Id", example = "CL123212", required = true)
  @Size(min = 1, max = 12, message = "The client transaction ID must be between 1 and 12 characters")
  val clientTransactionId: String,

  @Schema(description = "Client Name", example = "CL123212")
  val clientName: String?,

  @Schema(
    description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed",
    example = "CLIENT121131-0_11",
  )
  @Size(min = 1, max = 64, message = "The client unique reference must be between 1 and 64 characters")
  @Pattern(
    regexp = "[a-zA-Z0-9-_]+",
    message = "The client unique reference can only contain letters, numbers, hyphens and underscores",
  )
  val clientUniqueReference: String,
)

@Schema(description = "Hold Transaction to Release and Create Transaction")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReleaseHoldAndCreateTransaction(
  @Schema(
    description = "Valid transaction type for the prison_id",
    example = "CANT",
    allowableValues = ["CANT", "REFND", "PHONE", "MRPR", "MTDS", "DTDS", "CASHD", "RELA", "RELS"],
    required = true,
  )
  val type: String,

  @Schema(description = "Description of the Remove Hold Transaction", example = "Hold for Food", defaultValue = "Remove Hold")
  @Size(min = 1, max = 240, message = "The description must be between 1 and 240 characters")
  val removeDescription: String = "Remove Hold",

  @Schema(description = "Description of the Create Transaction", example = "Hold for Food")
  @Size(min = 1, max = 240, message = "The description must be between 1 and 240 characters")
  val createDescription: String,

  @Schema(description = "Client Transaction Id", example = "CL123212", required = true)
  @Size(min = 1, max = 12, message = "The client transaction ID must be between 1 and 12 characters")
  val clientTransactionId: String,

  @Schema(description = "Client Name", example = "CL123212")
  val clientName: String?,

  @Schema(
    description = "A reference unique to the client for the remove hold. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed",
    example = "CLIENT121131-0_11",
  )
  @Size(min = 1, max = 64, message = "The client unique reference must be between 1 and 64 characters")
  @Pattern(
    regexp = "[a-zA-Z0-9-_]+",
    message = "The client unique reference can only contain letters, numbers, hyphens and underscores",
  )
  val removeClientUniqueReference: String,

  @Schema(
    description = "A reference unique to the client for the create. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed",
    example = "CLIENT121131-0_11",
  )
  @Size(min = 1, max = 64, message = "The client unique reference must be between 1 and 64 characters")
  @Pattern(
    regexp = "[a-zA-Z0-9-_]+",
    message = "The client unique reference can only contain letters, numbers, hyphens and underscores",
  )
  val createClientUniqueReference: String,
) {
  fun toReleaseHold(): ReleaseHoldTransaction = ReleaseHoldTransaction(
    description = removeDescription,
    clientTransactionId = clientTransactionId,
    clientName = clientName,
    clientUniqueReference = removeClientUniqueReference,
  )
}

data class HoldDetails(
  val holdNumber: Long,
)
