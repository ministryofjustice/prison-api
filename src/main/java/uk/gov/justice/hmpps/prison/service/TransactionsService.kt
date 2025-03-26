package uk.gov.justice.hmpps.prison.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.util.MoneySupport
import uk.gov.justice.hmpps.prison.values.AccountCode
import java.time.LocalDate

@Service
class TransactionsService(
  private val offenderTransactionRepository: OffenderTransactionRepository,
  private val offenderRepository: OffenderRepository,
) {
  fun getAccountTransactions(
    prisonId: String,
    nomsId: String,
    accountCode: String,
    fromDate: LocalDate,
    toDate: LocalDate?,
  ): List<PrisonerTransaction> {
    val accountType = AccountCode.codeForNameOrEmpty(accountCode).orElseThrow {
      HttpClientErrorException(
        HttpStatus.BAD_REQUEST,
        "Invalid account_code supplied. Should be one of cash, spends or savings",
      )
    }
    val rootOffender = offenderRepository.findRootOffenderByNomsId(nomsId)
      .orElseThrow { HttpClientErrorException(HttpStatus.NOT_FOUND, "Offender not found") }

    return offenderTransactionRepository.findAccountTransactions(
      rootOffender.rootOffenderId,
      prisonId,
      accountType,
      fromDate,
      toDate,
    )
      .map {
        PrisonerTransaction(
          id = "${it.transactionId}-${it.transactionEntrySequence}",
          type = it.transactionType?.toCodeDescription(),
          description = it.entryDescription,
          amount = MoneySupport.poundsToPence(it.entryAmount).let { a -> a * (if (it.postingType == "DR") -1 else 1) },
          date = it.entryDate,
          clientUniqueRef = it.clientUniqueRef,
        )
      }
  }
}

fun TransactionType.toCodeDescription(): CodeDescription = CodeDescription.safeNullBuild(type, description)

@JsonInclude(Include.NON_NULL)
data class PrisonerTransaction(
  @Schema(description = "Transaction ID", requiredMode = RequiredMode.REQUIRED, example = "204564839-3")
  val id: String,

  @Schema(
    description = "The type of transaction",
    requiredMode = RequiredMode.REQUIRED,
  )
  val type: CodeDescription?,

  @Schema(
    description = "Transaction description",
    example = "Transfer In Regular from caseload PVR",
    requiredMode = RequiredMode.REQUIRED,
  )
  val description: String,

  @Schema(description = "Amount in pence", example = "12345", requiredMode = RequiredMode.REQUIRED)
  val amount: Long,

  @Schema(description = "Date of the transaction", example = "2016-10-21", requiredMode = RequiredMode.REQUIRED)
  val date: LocalDate,

  @Schema(
    description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed",
    example = "CLIENT121131-0_11",
  )
  val clientUniqueRef: String?,
)
