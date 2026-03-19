package uk.gov.justice.hmpps.prison.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription
import uk.gov.justice.hmpps.prison.api.resource.AddHoldRequest
import uk.gov.justice.hmpps.prison.api.resource.FinanceHold
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.FinanceHoldsRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.util.MoneySupport
import uk.gov.justice.hmpps.prison.util.MoneySupport.penceToPounds
import uk.gov.justice.hmpps.prison.values.AccountCode
import java.time.LocalDate

@Service
class TransactionsService(
  private val offenderTransactionRepository: OffenderTransactionRepository,
  private val offenderRepository: OffenderRepository,
  private val financeHoldsRepository: FinanceHoldsRepository,
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
          id = "${it.id.transactionId}-${it.id.transactionEntrySequence}",
          type = it.transactionType.toCodeDescription(),
          description = it.entryDescription!!,
          amount = MoneySupport.poundsToPence(it.entryAmount).let { a -> a * (if (it.postingType == "DR") -1 else 1) },
          date = it.entryDate,
          clientUniqueRef = it.clientUniqueRef,
        )
      }
  }

  fun addHold(prisonId: String, nomisId: String, holdRequest: AddHoldRequest): FinanceHold {
    val rootOffender = offenderRepository.findRootOffenderByNomsId(nomisId)
      .orElseThrow { HttpClientErrorException(HttpStatus.NOT_FOUND, "Offender not found") }

    val accountCode = AccountCode.byCodeName(holdRequest.subAccountType)
      .orElseThrow { HttpClientErrorException(HttpStatus.NOT_FOUND, "SubAccountCode not found") }

    return financeHoldsRepository.addHold(
      prisonId = prisonId,
      nomisId = nomisId,
      rootOffenderId = rootOffender.rootOffenderId,
      accountCode = accountCode.code,
      amount = penceToPounds(holdRequest.amount),
      holdDescription = holdRequest.holdDescription,
    )
  }

  fun removeHold(prisonId: String, nomisId: String, holdNumber: Long) {
    val rootOffender = offenderRepository.findRootOffenderByNomsId(nomisId)
      .orElseThrow { HttpClientErrorException(HttpStatus.NOT_FOUND, "Offender not found") }

    financeHoldsRepository.removeHold(prisonId, nomisId, rootOffender.id, holdNumber)
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
