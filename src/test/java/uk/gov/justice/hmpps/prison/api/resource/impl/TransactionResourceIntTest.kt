package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class TransactionResourceIntTest : ResourceTest() {
  @Nested
  @DisplayName("GET /api/transactions/prison/{prison_id}/offenders/{noms_id}/accounts/{account_code}")
  inner class GetTransactions {

    @Nested
    @DisplayName("Authorisation checks")
    inner class Authorisation {
      @Test
      fun `returns 401 without an auth token`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/spends")
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/spends")
          .headers(setClientAuthorisation(listOf()))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 when supplied roles do not include PRISON_API__HMPPS_INTEGRATION_API`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/spends")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 200 when supplied role includes PRISON_API__HMPPS_INTEGRATION_API`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/spends")
          .headers(setClientAuthorisation(listOf("PRISON_API__HMPPS_INTEGRATION_API")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("Happy path")
    open inner class HappyPath {

      @Test
      open fun `should get account transactions`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/spends")
          .headers(setClientAuthorisation(listOf("PRISON_API__HMPPS_INTEGRATION_API")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.transactions.length()").isEqualTo(0)
      }

      @Test
      open fun `should get account transactions with from date specified`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/spends?from_date=2019-01-01")
          .headers(setClientAuthorisation(listOf("PRISON_API__HMPPS_INTEGRATION_API")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.transactions.length()").isEqualTo(1)
          .jsonPath("$.transactions[0].id").isEqualTo("301826808-3")
          .jsonPath("$.transactions[0].type.code").isEqualTo("AD")
          .jsonPath("$.transactions[0].type.desc").isEqualTo("Open/Reopen Trust Account")
          .jsonPath("$.transactions[0].description").isEqualTo("DESC")
          .jsonPath("$.transactions[0].amount").isEqualTo(100)
          .jsonPath("$.transactions[0].date").isEqualTo("2019-10-17")
          .jsonPath("$.transactions[0].clientUniqueRef").isEqualTo("mtp-prod-520831")
      }

      @Test
      open fun `should get account transactions with to date specified`() {
        webTestClient.get()
          .uri("/api/transactions/prison/LEI/offenders/A1234AI/accounts/cash?from_date=2019-01-01&to_date=2019-10-19")
          .headers(setClientAuthorisation(listOf("PRISON_API__HMPPS_INTEGRATION_API")))
          .header("Content-Type", APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.transactions.length()").isEqualTo(1)
          .jsonPath("$.transactions[0].id").isEqualTo("301826806-1")
          .jsonPath("$.transactions[0].type.code").isEqualTo("AD")
          .jsonPath("$.transactions[0].type.desc").isEqualTo("Open/Reopen Trust Account")
          .jsonPath("$.transactions[0].description").isEqualTo("DESC")
          .jsonPath("$.transactions[0].amount").isEqualTo(-100)
          .jsonPath("$.transactions[0].date").isEqualTo("2019-10-17")
          .jsonPath("$.transactions[0].clientUniqueRef").isEqualTo("mtp-prod-520829")
      }
    }
  }
}
