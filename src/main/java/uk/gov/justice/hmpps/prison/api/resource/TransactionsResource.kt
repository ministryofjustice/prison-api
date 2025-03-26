package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.AccountTransaction
import uk.gov.justice.hmpps.prison.service.TransactionsService
import java.time.LocalDate

@RestController
@Validated
@RequestMapping(value = ["/api/transactions"], produces = ["application/json"])
class TransactionsResource(private val transactionsService: TransactionsService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Not a digital prison.  Prison not found. Offender has no account at this prison.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Prison, offender or accountType not found",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Retrieve an offender's financial transaction history for cash, spends or savings.",
    description = """
            Transactions are returned in NOMIS order (Descending date followed by id).<br/>
            All transaction amounts are represented as pence values.<br/>
            Requires UNILINK or PRISON_API__HMPPS_INTEGRATION_API role.
            """,
  )
  @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts/{account_code}")
  @Tag(name = "integration-api")
  @Tag(name = "unilink")
  @PreAuthorize("hasAnyRole('UNILINK', 'PRISON_API__HMPPS_INTEGRATION_API')")
  fun getAccountTransactions(
    @PathVariable("prison_id") @Parameter(
      name = "prison_id",
      description = "Prison ID",
      example = "WLI",
      required = true,
    ) prisonId:
    @Size(max = 3)
    @NotNull
    String,
    @PathVariable("noms_id") @Parameter(
      name = "noms_id",
      description = "Offender Noms Id",
      example = "A1404AE",
      required = true,
    ) nomsId:
    @Pattern(regexp = NomisApiV1Resource.NOMS_ID_REGEX_PATTERN)
    @NotNull
    String,
    @PathVariable("account_code") @Parameter(
      name = "account_code",
      description = "Account code",
      example = "spends",
      required = true,
      schema = Schema(implementation = String::class, allowableValues = ["spends", "cash", "savings"]),
    ) accountCode: @NotNull String,
    @RequestParam(
      value = "from_date",
      required = false,
    ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(
      name = "from_date",
      description = "Start date for transactions (defaults to today if not supplied)",
      example = "2019-04-01",
    ) fromDate: LocalDate?,
    @RequestParam(
      value = "to_date",
      required = false,
    ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(
      name = "to_date",
      description = "To date for transactions",
      example = "2019-05-01",
    ) toDate: LocalDate?,
  ): AccountTransactions = AccountTransactions(transactionsService.getAccountTransactions(prisonId, nomsId, accountCode, fromDate ?: LocalDate.now(), toDate))
}

@Schema(description = "Account Transactions")
data class AccountTransactions(
  @Schema(description = "List of account transactions")
  val transactions: List<AccountTransaction>,
)
