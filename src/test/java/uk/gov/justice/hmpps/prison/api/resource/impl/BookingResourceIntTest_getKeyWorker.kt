@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GET /api/bookings/offenderNo/{offenderNo}/key-worker")
class BookingResourceIntTest_getKeyWorker : ResourceTest() {

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Nested
    inner class ClientAccess {

      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
      }

      @Test
      fun `returns 403 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
      }

      @Test
      fun `returns success if has override ROLE_GLOBAL_SEARCH`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns success if has override ROLE_VIEW_PRISONER_DATA`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 200 if has override ROLE_KEY_WORKER`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_KEY_WORKER")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 403 if has role ROLE_SYSTEM_USER`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `returns 404 if offender does not exist`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A9999ZZ/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER"))).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A9999ZZ] not found.")
      }

      @Test
      fun `returns 404 if no key worker`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A4476RS/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Key worker not found for booking Id -30")
      }
    }

    @Nested
    inner class UserAccess {

      @Test
      fun `returns 403 when user does not have any caseloads`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf()))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
      }

      @Test
      fun `returns 403 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
      }

      @Test
      fun `returns success if has override ROLE_GLOBAL_SEARCH`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns success if has override ROLE_VIEW_PRISONER_DATA`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 404 if offender does not exist`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234ZZ/key-worker")
          .headers(setAuthorisation(listOf())).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1234ZZ] not found.")
      }

      @Test
      fun `returns 404 if no bookings`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AO/key-worker")
          .headers(setAuthorisation("RO_USER", listOf())).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("No bookings found for offender A1234AO")
      }

      @Test
      fun `returns 404 if no key worker`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A4476RS/key-worker")
          .headers(setAuthorisation("ITAG_USER", listOf())).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Key worker not found for booking Id -30")
      }

      @Test
      fun `returns 200 if key worker`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("ITAG_USER", listOf(""))).exchange().expectStatus().isOk
      }
    }
  }
}
