package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.resource.HoldAccountCode
import uk.gov.justice.hmpps.prison.api.resource.HoldTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class FinanceHoldsControllerTest : ResourceTest() {
  @MockitoBean
  private lateinit var processGlTransNew: ProcessGlTransNew

  @MockitoBean
  private lateinit var updateOffenderBalance: TrustProcs.UpdateOffenderBalance

  @MockitoBean
  private lateinit var offenderTransactionRepository: OffenderTransactionRepository

  @Nested
  inner class AddHold {
    private val transaction = HoldTransaction(
      amount = 134L,
      clientUniqueRef = "clientRef",
      description = "desc",
      clientTransactionId = "transId",
      accountCode = HoldAccountCode.SPENDS,
      clientName = "clientName",
    )

    @Nested
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when invalid role`() {
        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class Validation {

      @Test
      fun validatePrisonId() {
        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "1234", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage").isEqualTo("addHold.prisonId: Value is too long: max length is 3")
          .jsonPath("developerMessage").isEqualTo("addHold.prisonId: Value is too long: max length is 3")
      }

      @Test
      fun validateOffenderNo() {
        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "123ABC")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("addHold.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
          .jsonPath("developerMessage")
          .isEqualTo("addHold.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
      }

      @Test
      fun validateTransactionAmount() {
        val transaction = transaction.copy(amount = 0)

        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage").isEqualTo("Field: amount - The amount must be greater than 0")
          .jsonPath("developerMessage").isEqualTo("Field: amount - The amount must be greater than 0")
      }

      @Test
      fun validateTransactionDescription() {
        val transaction = transaction.copy(description = "")

        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage").isEqualTo("Field: description - The description must be between 1 and 240 characters")
          .jsonPath("developerMessage").isEqualTo("Field: description - The description must be between 1 and 240 characters")
      }

      @Test
      fun validateTransactionClientTransactionId() {
        val transaction = transaction.copy(clientTransactionId = "")

        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage").isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
          .jsonPath("developerMessage").isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
      }

      @Test
      fun validateTransactionClientUniqueRef() {
        val transaction = transaction.copy(clientUniqueRef = "")

        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: clientUniqueRef - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: clientUniqueRef - The client unique reference must be between 1 and 64 characters")
          }
          .jsonPath("developerMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: clientUniqueRef - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: clientUniqueRef - The client unique reference must be between 1 and 64 characters")
          }
      }

      @Test
      fun validateTransactionAccountCode() {
        val transaction =
          """
            {
              "amount": 134,
              "clientUniqueRef": "clientRef",
              "description": "desc",
              "clientTransactionId": "transId",
              "accountCode": "fred",
              "clientName": "clientName"
            }
          """.trimIndent()

        webTestClient.post()
          .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
      }
    }

    @Test
    fun happyPath() {
      whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)
      whenever(offenderTransactionRepository.findById(ArgumentMatchers.any()))
        .thenReturn(Optional.of(offenderTransaction()))
      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("holdNumber").isEqualTo(12345)
    }

    @Test
    fun setXClientNameHeader() {
      whenever(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L)

      webTestClient.post()
        .uri("/api/finance/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .header("X-Client-Name", "clientName")
        .headers(setClientAuthorisation(listOf("ROLE_NOMIS_CMS_API")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isOk

      verify(offenderTransactionRepository).save(
        check {
          assertThat(it.clientUniqueRef).isEqualTo("clientName-clientRef")
        },
      )
    }
  }
}

fun offenderTransaction(
  id: OffenderTransactionId = OffenderTransactionId(1, 1),
) = OffenderTransaction(
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
  postingType = "CR",
  offenderBookingId = 1,
  slipPrintedFlag = false,
  modifyDate = LocalDateTime.now(),
)
