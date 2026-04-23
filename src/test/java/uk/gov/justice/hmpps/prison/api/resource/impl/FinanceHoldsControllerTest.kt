package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.resource.AddHoldTransaction
import uk.gov.justice.hmpps.prison.api.resource.HoldDetails
import uk.gov.justice.hmpps.prison.api.resource.ReleaseHoldAndCreateTransaction
import uk.gov.justice.hmpps.prison.api.resource.ReleaseHoldTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs
import uk.gov.justice.hmpps.prison.repository.v1.FinanceV1Repository
import java.math.BigDecimal
import java.time.LocalDate

class FinanceHoldsControllerTest : ResourceTest() {

  @MockitoBean
  private lateinit var processGlTransNew: TrustProcs.ProcessGlTransNew

  @MockitoBean
  private lateinit var updateOffenderBalance: TrustProcs.UpdateOffenderBalance

  @MockitoBean
  private lateinit var financeV1Repository: FinanceV1Repository

  @Autowired
  private lateinit var offenderTransactionRepository: OffenderTransactionRepository

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @AfterEach
  fun tearDown() {
    jdbcTemplate.update("DELETE FROM OFFENDER_TRANSACTIONS WHERE client_unique_ref like 'clientRefHoldTest%';")
  }

  @Nested
  inner class AddHold {
    private val transaction = AddHoldTransaction(
      amount = 134L,
      clientUniqueReference = "clientRefHoldTestAdd",
      description = "desc",
      clientTransactionId = "transId",
      clientName = "clientName",
    )

    @Nested
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when invalid role`() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
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
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "1234", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
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
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "123ABC")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
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
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
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
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: description - The description must be between 1 and 240 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: description - The description must be between 1 and 240 characters")
      }

      @Test
      fun validateTransactionClientTransactionId() {
        val transaction = transaction.copy(clientTransactionId = "")

        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
      }

      @Test
      fun validateTransactionClientUniqueReference() {
        val transaction = transaction.copy(clientUniqueReference = "")

        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: clientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: clientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
          .jsonPath("developerMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: clientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: clientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
      }
    }

    @Test
    fun happyPath() {
      webTestClient.post()
        .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("holdNumber").value<Int> { assertThat(it).isGreaterThan(0) }
    }
  }

  @Nested
  inner class ReleaseHold {
    private val transaction = ReleaseHoldTransaction(
      clientUniqueReference = "clientRefRelTest",
      description = "desc",
      clientTransactionId = "transId",
      clientName = "clientName",
    )

    @Nested
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", 343)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", 343)
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when invalid role`() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", 343)
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
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "1234", "A1234AA", 343)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage").isEqualTo("releaseHold.prisonId: Value is too long: max length is 3")
          .jsonPath("developerMessage").isEqualTo("releaseHold.prisonId: Value is too long: max length is 3")
      }

      @Test
      fun validateOffenderNo() {
        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "123ABC", 343)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("releaseHold.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
          .jsonPath("developerMessage")
          .isEqualTo("releaseHold.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
      }

      @Test
      fun validateTransactionDescription() {
        val transaction = transaction.copy(description = "")

        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", 343)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: description - The description must be between 1 and 240 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: description - The description must be between 1 and 240 characters")
      }

      @Test
      fun validateTransactionClientTransactionId() {
        val transaction = transaction.copy(clientTransactionId = "")

        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", 343)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
      }

      @Test
      fun validateTransactionClientUniqueReference() {
        val transaction = transaction.copy(clientUniqueReference = "")

        webTestClient.post()
          .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", 343)
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: clientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: clientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
          .jsonPath("developerMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: clientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: clientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
      }
    }

    @Test
    fun happyPath() {
      val transactionToRelease = AddHoldTransaction(
        amount = 124L,
        clientUniqueReference = "clientRefHoldTestRem",
        description = "desc",
        clientTransactionId = "transId",
        clientName = "clientName",
      )

      val hold = webTestClient.post()
        .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(transactionToRelease)
        .exchange()
        .expectStatus().isCreated
        .expectBody(HoldDetails::class.java)
        .returnResult()
        .responseBody!!

      webTestClient.post()
        .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}", "LEI", "A1234AA", hold.holdNumber)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isCreated
    }
  }

  @Nested
  inner class ReleaseHoldCreateTransaction {
    private val transaction = ReleaseHoldAndCreateTransaction(
      type = "CANT",
      removeClientUniqueReference = "clientRefHoldTestRemCant",
      removeDescription = "desc",
      createClientUniqueReference = "createClientRef",
      createDescription = "new",
      clientTransactionId = "transId",
      clientName = "clientName",
    )

    @Nested
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when invalid role`() {
        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
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
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "1234",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("releaseHoldAndCreateTransaction.prisonId: Value is too long: max length is 3")
          .jsonPath("developerMessage")
          .isEqualTo("releaseHoldAndCreateTransaction.prisonId: Value is too long: max length is 3")
      }

      @Test
      fun validateOffenderNo() {
        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "123ABC",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("releaseHoldAndCreateTransaction.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
          .jsonPath("developerMessage")
          .isEqualTo("releaseHoldAndCreateTransaction.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'")
      }

      @Test
      fun validateTransactionCreateDescription() {
        val transaction = transaction.copy(createDescription = "")

        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: createDescription - The description must be between 1 and 240 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: createDescription - The description must be between 1 and 240 characters")
      }

      @Test
      fun validateTransactionRemoveDescription() {
        val transaction = transaction.copy(removeDescription = "")

        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: removeDescription - The description must be between 1 and 240 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: removeDescription - The description must be between 1 and 240 characters")
      }

      @Test
      fun validateTransactionClientTransactionId() {
        val transaction = transaction.copy(clientTransactionId = "")

        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
          .jsonPath("developerMessage")
          .isEqualTo("Field: clientTransactionId - The client transaction ID must be between 1 and 12 characters")
      }

      @Test
      fun validateTransactionCreateClientUniqueReference() {
        val transaction = transaction.copy(removeClientUniqueReference = "")

        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: removeClientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: removeClientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
          .jsonPath("developerMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: removeClientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: removeClientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
      }

      @Test
      fun validateTransactionRemoveClientUniqueReference() {
        val transaction = transaction.copy(createClientUniqueReference = "")

        webTestClient.post()
          .uri(
            "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
            "LEI",
            "A1234AA",
            343,
          )
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
          .bodyValue(transaction)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("status").isEqualTo("400")
          .jsonPath("userMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: createClientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: createClientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
          .jsonPath("developerMessage")
          .value<String> { message ->
            assertThat(message)
              .contains("Field: createClientUniqueReference - The client unique reference can only contain letters, numbers, hyphens and underscores")
              .contains("Field: createClientUniqueReference - The client unique reference must be between 1 and 64 characters")
          }
      }
    }

    @Test
    fun validateTransactionCreateAndRemoveClientUniqueReferenceDifferent() {
      val transaction = transaction.copy(createClientUniqueReference = "billy", removeClientUniqueReference = "billy")

      webTestClient.post()
        .uri(
          "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
          "LEI",
          "A1234AA",
          343,
        )
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("status").isEqualTo("400")
        .jsonPath("userMessage")
        .value<String> { message ->
          assertThat(message)
            .contains("Remove and create client unique references cannot be the same: billy")
        }
        .jsonPath("developerMessage")
        .value<String> { message ->
          assertThat(message)
            .contains("Remove and create client unique references cannot be the same: billy")
        }
    }

    @Test
    fun happyPath() {
      val transactionToRelease = AddHoldTransaction(
        amount = 125L,
        clientUniqueReference = "clientRefHoldTest",
        description = "desc",
        clientTransactionId = "transId",
        clientName = "clientName",
      )
      val hold = webTestClient.post()
        .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", "LEI", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(transactionToRelease)
        .exchange()
        .expectStatus().isCreated
        .expectBody(HoldDetails::class.java)
        .returnResult()
        .responseBody!!

      whenever(
        financeV1Repository.postTransaction(
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
        ),
      ).thenReturn("billyBob")

      webTestClient.post()
        .uri(
          "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
          "LEI",
          "A1234AA",
          hold.holdNumber,
        )
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CANTEEN_FUNDS_API__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(transaction)
        .exchange()
        .expectStatus().isCreated
        .expectBody().jsonPath(".id").isEqualTo("billyBob")

      verify(financeV1Repository).postTransaction(
        "LEI",
        "A1234AA",
        "CANT",
        transaction.createDescription,
        BigDecimal.valueOf(1.25),
        LocalDate.now(),
        "transId",
        "createClientRef",
      )
    }
  }
}
