@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify

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
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -1.")
      }

      @Test
      fun `returns 403 when client as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -1.")
      }

      @Test
      fun `returns success as ROLE_GLOBAL_SEARCH is override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns success as ROLE_VIEW_PRISONER_DATA is override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 200 when client has override role ROLE_VISIT_SCHEDULER`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_VISIT_SCHEDULER")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `invalid client access produces telemetry event`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isForbidden

        verify(telemetryClient).trackEvent(eq("ClientUnauthorisedBookingAccess"), any(), isNull())
      }
    }

    @Nested
    inner class UserAccess {
      @Test
      fun `should return 403 when user does not have any caseloads`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 403 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns success as ROLE_GLOBAL_SEARCH is override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AB/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns success as ROLE_VIEW_PRISONER_DATA is override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 403 if not in user caseload`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("WAI_USER", listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 404 if offender does not exist`() {
        webTestClient.get().uri("/api/bookings/offenderNo/Z9999ZZ/visit/balances")
          .headers(setAuthorisation("PRISON_API_USER", listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [Z9999ZZ] not found.")
      }

      @Test
      fun `returns 200 if in user caseload`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/visit/balances")
          .headers(setAuthorisation("ITAG_USER", listOf()))
          .exchange()
          .expectStatus().isOk
      }
    }
  }
}
