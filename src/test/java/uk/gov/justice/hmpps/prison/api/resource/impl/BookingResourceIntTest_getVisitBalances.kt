@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookingResourceIntTest_getVisitBalances : ResourceTest() {
  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Nested
    inner class ClientAccess {
      @Test
      fun `returns 404 when client does not have any roles`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 when client as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          // RO_USER has no caseloads
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 as ROLE_GLOBAL_SEARCH is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AC/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
      }

      @Test
      fun `returns 404 as ROLE_VIEW_PRISONER_DATA is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 200 when client has override ROLE_SYSTEM_USER`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    inner class UserAccess {
      @Test
      fun `should return 404 when user does not have any roles`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          // RO_USER has no caseloads
          .headers(setAuthorisation("RO_USER", listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 as ROLE_GLOBAL_SEARCH is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AC/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
      }

      @Test
      fun `returns 404 as ROLE_VIEW_PRISONER_DATA is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 if not in user caseload`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AE/visit/balances")
          .headers(setAuthorisation("ITAG_USER", listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [-5] not found.")
      }

      @Test
      fun `returns 200 if in user caseload`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .exchange()
          .expectStatus().isOk
      }
    }
  }
}
