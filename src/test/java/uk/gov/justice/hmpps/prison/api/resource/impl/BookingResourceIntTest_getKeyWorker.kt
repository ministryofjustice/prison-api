@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
      fun `returns 404 when client does not have any roles`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 if has override ROLE_GLOBAL_SEARCH`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 if has override ROLE_VIEW_PRISONER_DATA`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 200 if has override ROLE_SYSTEM_USER`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isOk
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
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER"))).exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Key worker not found for booking Id -30")
      }
    }

    @Nested
    inner class UserAccess {

      @Test
      fun `returns 404 when user does not have any roles`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 if has override ROLE_GLOBAL_SEARCH`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 if has override ROLE_VIEW_PRISONER_DATA`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/key-worker")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
      }

      @Test
      fun `returns 404 if offender does not exist`() {
        webTestClient.get().uri("/api/bookings/offenderNo/A1234ZZ/key-worker")
          .headers(setAuthorisation("RO_USER", listOf())).exchange()
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