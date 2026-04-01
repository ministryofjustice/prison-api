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
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.FinanceHoldsService
import uk.gov.justice.hmpps.prison.util.ResourceUtils

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
  @Operation(
    summary = "Add a hold financial transaction to NOMIS.",
    description = """
      Add a financial hold to an offender’s account, reserving funds so that a future canteen transaction can be completed successfully.
      Used by the CMS replacement team to support canteen ordering.
      Requires PRISON_API__CANTEEN_FUNDS_API__RW role.
      """,
  )
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "Hold Added"),
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
  ): HoldDetails {
    val clientUniqueId = ResourceUtils.getUniqueClientId(holdTransaction.clientName, holdTransaction.clientUniqueReference)
    return financeHoldsService.addHold(prisonId, offenderNo, holdTransaction, clientUniqueId)
  }

  @PostMapping("/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}")
  @Operation(
    summary = "Remove a hold from an existing hold financial transaction to NOMIS.",
    description = """
      Removes a financial hold on an offender’s account, making funds available so that a canteen transaction can be completed successfully.
      Used by the CMS replacement team to support canteen ordering.
      Requires PRISON_API__CANTEEN_FUNDS_API__RW role.
      """,
  )
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "Hold Removed"),
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
    val clientUniqueId = ResourceUtils.getUniqueClientId(holdTransaction.clientName, holdTransaction.clientUniqueReference)
    financeHoldsService.releaseHold(prisonId, offenderNo, holdTransaction, clientUniqueId, holdNumber)
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

  @Schema(description = "Client Name", example = "CL123212", required = false)
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

  @Schema(description = "Client Name", example = "CL123212", required = false)
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

data class HoldDetails(
  val holdNumber: Long,
)
