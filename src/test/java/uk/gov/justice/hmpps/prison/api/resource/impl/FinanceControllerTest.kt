package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod.POST
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew
import java.util.Optional

class FinanceControllerTest : ResourceTest() {
  @MockBean
  private lateinit var insertIntoOffenderTrans: InsertIntoOffenderTrans

  @MockBean
  private lateinit var processGlTransNew: ProcessGlTransNew

  @MockBean
  private lateinit var offenderTransactionRepository: OffenderTransactionRepository

  @Test
  fun transferToSavingsNomisV1Role() {
    whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
    whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
      .thenReturn(Optional.of(OffenderTransaction.builder().build()))
      .thenReturn(Optional.of(OffenderTransaction.builder().build()))
    val transaction = createTransferTransaction(124L)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "LEI",
      "A1234AA",
    )
    assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":12345}")
  }

  @Test
  fun transferToSavingsUnilinkRole() {
    whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
    whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
      .thenReturn(Optional.of(OffenderTransaction.builder().build()))
      .thenReturn(Optional.of(OffenderTransaction.builder().build()))
    val transaction = createTransferTransaction(124L)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("UNILINK"), transaction)
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "LEI",
      "A1234AA",
    )
    assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":12345}")
  }

  @Test
  fun transferToSavings_setXClientNameHeader() {
    whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
    val transaction1 = OffenderTransaction.builder().build()
    whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
      .thenReturn(Optional.of(transaction1))
      .thenReturn(Optional.of(OffenderTransaction.builder().build()))
    val transaction = createTransferTransaction(124L)
    val jwt = createJwt("ITAG_USER", listOf("ROLE_NOMIS_API_V1"))
    val requestEntity = createHttpEntity(jwt, transaction, mapOf("X-Client-Name" to "clientName"))
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "LEI",
      "A1234AA",
    )
    assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":12345}")
    Assertions.assertThat(transaction1.clientUniqueRef).isEqualTo("clientName-clientRef")
  }

  @Test
  fun transferToSavings_validatePrisonId() {
    val transaction = createTransferTransaction(12345678L)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "1234",
      "A1234AA",
    )
    assertThatJsonAndStatus(
      responseEntity,
      400,
      "{\"status\":400,\"userMessage\":\"transferToSavings.prisonId: Value is too long: max length is 3\",\"developerMessage\":\"transferToSavings.prisonId: Value is too long: max length is 3\"}",
    )
  }

  @Test
  fun transferToSavings_validateOffenderNo() {
    val transaction = createTransferTransaction(12345678L)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "LEI",
      "123ABC",
    )
    assertThatJsonAndStatus(
      responseEntity,
      400,
      "{\"status\":400,\"userMessage\":\"transferToSavings.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'\",\"developerMessage\":\"transferToSavings.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'\"}",
    )
  }

  @Test
  fun transferToSavings_validateTransferTransaction() {
    val transaction = createTransferTransaction(0L)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "LEI",
      "A1234AA",
    )
    assertThatJsonAndStatus(
      responseEntity,
      400,
      "{\"status\":400,\"userMessage\":\"Field: amount - The amount must be greater than 0\",\"developerMessage\":\"Field: amount - The amount must be greater than 0\"}",
    )
  }

  @Test
  fun transferToSavings_wrongRole() {
    val transaction = createTransferTransaction(1L)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_BOB"), transaction)
    val responseEntity = testRestTemplate.exchange(
      "/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
      POST,
      requestEntity,
      String::class.java,
      "LEI",
      "A1234AA",
    )
    assertThatJsonAndStatus(responseEntity, 403, "{\"status\":403,\"userMessage\":\"Access Denied\"}")
  }

  private fun createTransferTransaction(amount: Long): TransferTransaction {
    return TransferTransaction.builder()
      .amount(amount)
      .clientUniqueRef("clientRef")
      .description("desc")
      .clientTransactionId("transId")
      .build()
  }
}
