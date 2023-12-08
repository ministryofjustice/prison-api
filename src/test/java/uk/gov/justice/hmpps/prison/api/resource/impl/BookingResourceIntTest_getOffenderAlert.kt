@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GET /api/bookings/{bookingId}/alerts/{alertId}")
class BookingResourceIntTest_getOffenderAlert : ResourceTest() {

  @Nested
  inner class Authorisation {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 404 when user does not have any caseloads`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -4 not found.")
    }

    @Test
    fun `returns 404 as ROLE_BANANAS is not override role`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -4 not found.")
    }

    @Test
    fun `returns 404 as ROLE_SYSTEM_USER is not override role`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -4 not found.")
    }

    @Test
    fun `returns success if ROLE_GLOBAL_SEARCH is an override role`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success if ROLE_VIEW_PRISONER_DATA is an override role`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -4 not found.")
    }

    @Test
    fun `returns 404 if booking does not exist`() {
      webTestClient.get().uri("/api/bookings/-99/alerts/-1")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99 not found.")
    }

    @Test
    fun `returns 200 if in user caseload`() {
      webTestClient.get().uri("/api/bookings/-1/alerts/4")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 200 and correct data if booking -1 in user caseloads`() {
      webTestClient.get().uri("/api/bookings/-4/alerts/1")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("alertId").isEqualTo(1)
        .jsonPath("alertType").isEqualTo("R")
        .jsonPath("alertTypeDescription").isEqualTo("Risk")
        .jsonPath("alertCode").isEqualTo("ROM")
        .jsonPath("alertCodeDescription").isEqualTo("OASys Serious Harm-Medium")
        .jsonPath("comment").isEqualTo("Alert Text 4")
        .jsonPath("dateExpires").doesNotExist()
        .jsonPath("expired").isEqualTo("false")
    }

    @Test
    fun `returns 200 and correct data if booking -8 in user caseloads`() {
      webTestClient.get().uri("/api/bookings/-8/alerts/1")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("alertId").isEqualTo(1)
        .jsonPath("alertType").isEqualTo("X")
        .jsonPath("alertTypeDescription").isEqualTo("Security")
        .jsonPath("alertCode").isEqualTo("XCU")
        .jsonPath("alertCodeDescription").isEqualTo("Controlled Unlock")
        .jsonPath("comment").isEqualTo("Alert Text 8")
        .jsonPath("dateExpires").doesNotExist()
        .jsonPath("expired").isEqualTo("false")
    }
  }
}
