package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.PostingType
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class FinanceControllerTest : ResourceTest() {
  @MockitoBean
  private lateinit var insertIntoOffenderTrans: InsertIntoOffenderTrans

  @MockitoBean
  private lateinit var processGlTransNew: ProcessGlTransNew

  @MockitoBean
  private lateinit var offenderTransactionRepository: OffenderTransactionRepository

  @Nested
  inner class TransferToSavings {
    @Test
    fun transferToSavingsNomisV1Role() {
      whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
      whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
        .thenReturn(Optional.of(offenderTransaction()))
        .thenReturn(Optional.of(offenderTransaction()))
      val transaction = createTransferTransaction(124L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("debitTransaction.id").isEqualTo("12345-1")
        .jsonPath("creditTransaction.id").isEqualTo("12345-2")
        .jsonPath("transactionId").isEqualTo("12345")
    }

    @Test
    fun transferToSavingsUnilinkRole() {
      whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
      whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
        .thenReturn(Optional.of(offenderTransaction()))
        .thenReturn(Optional.of(offenderTransaction()))
      val transaction = createTransferTransaction(124L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("UNILINK")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("debitTransaction.id").isEqualTo("12345-1")
        .jsonPath("creditTransaction.id").isEqualTo("12345-2")
        .jsonPath("transactionId").isEqualTo("12345")
    }

    @Test
    fun transferToSavings_setXClientNameHeader() {
      whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
      val transaction1 = offenderTransaction()
      whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
        .thenReturn(Optional.of(transaction1))
        .thenReturn(Optional.of(offenderTransaction()))
      val transaction = createTransferTransaction(124L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .header("X-Client-Name", "clientName")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("debitTransaction.id").isEqualTo("12345-1")
        .jsonPath("creditTransaction.id").isEqualTo("12345-2")
        .jsonPath("transactionId").isEqualTo("12345")

      assertThat(transaction1.clientUniqueRef).isEqualTo("clientName-clientRef")
    }

    @Test
    fun transferToSavings_validatePrisonId() {
      val transaction = createTransferTransaction(12345678L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "1234", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("status").isEqualTo("400")
        .jsonPath("userMessage").isEqualTo("transferToSavings.prisonId: Value is too long: max length is 3")
        .jsonPath("developerMessage").isEqualTo("transferToSavings.prisonId: Value is too long: max length is 3")
    }

    @Test
    fun transferToSavings_validateOffenderNo() {
      val transaction = createTransferTransaction(12345678L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "123ABC")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("status").isEqualTo("400")
        .jsonPath("userMessage")
        .isEqualTo("transferToSavings.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
        .jsonPath("developerMessage")
        .isEqualTo("transferToSavings.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
    }

    @Test
    fun transferToSavings_validateTransferTransaction() {
      val transaction = createTransferTransaction(0L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("status").isEqualTo("400")
        .jsonPath("userMessage").isEqualTo("Field: amount - The amount must be greater than 0")
        .jsonPath("developerMessage").isEqualTo("Field: amount - The amount must be greater than 0")
    }

    @Test
    fun transferToSavings_wrongRole() {
      val transaction = createTransferTransaction(1L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_BANANAS")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .jsonPath("status").isEqualTo("403")
        .jsonPath("userMessage").isEqualTo("Access Denied")
    }

    @Test
    fun transferToSavings_duplicateClientUniqueRef() {
      whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
        .thenReturn(Optional.of(offenderTransaction()))
        .thenReturn(Optional.of(offenderTransaction()))
      val transaction = createTransferTransaction(124L)
      whenever(offenderTransactionRepository.findByClientUniqueRef(any())).thenReturn(Optional.of(offenderTransaction()))

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("status").isEqualTo("409")
        .jsonPath("userMessage").isEqualTo("Duplicate post - The unique_client_ref clientRef has been used before")
        .jsonPath("developerMessage").isEqualTo("Duplicate post - The unique_client_ref clientRef has been used before")
    }

    private fun createTransferTransaction(amount: Long): TransferTransaction = TransferTransaction.builder()
      .amount(amount)
      .clientUniqueRef("clientRef")
      .description("desc")
      .clientTransactionId("transId")
      .build()
  }

  private fun offenderTransaction(id: OffenderTransactionId = OffenderTransactionId(1, 1)) = OffenderTransaction(
    id = id,
    offenderId = 1,
    prisonId = "BMI",
    holdNumber = null,
    holdClearFlag = null,
    subAccountType = "REG",
    transactionType = TransactionType("CANT", "Canteen"),
    transactionReferenceNumber = null,
    clientUniqueRef = null,
    entryDate = LocalDate.now(),
    entryDescription = null,
    entryAmount = BigDecimal.TEN,
    postingType = PostingType.CR,
    offenderBookingId = 1,
    slipPrintedFlag = false,
    modifyDate = LocalDateTime.now(),
  )
}
