@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class BookingResourceIntTest_getEvents : ResourceTest() {

  @Nested
  @TestInstance(PER_CLASS)
  inner class SecureEndpoints {
    private fun secureEndpoints() =
      listOf(
        "/api/bookings/{bookingId}/events",
        "/api/bookings/{bookingId}/events/today",
        "/api/bookings/{bookingId}/events/thisWeek",
        "/api/bookings/{bookingId}/events/nextWeek",
      )

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    internal fun `requires a valid authentication token`(uri: String) {
      webTestClient.get()
        .uri(uri, -1)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    internal fun `requires the correct role`(uri: String) {
      webTestClient.get()
        .uri(uri, -1)
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    fun `returns 404 if booking not found`(uri: String) {
      webTestClient.get()
        .uri(uri, -99999)
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    internal fun `returns 404 if not in user caseload`(uri: String) {
      webTestClient.get()
        .uri(uri, -1)
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    internal fun `returns success when client has override role ROLE_GLOBAL_SEARCH`(uri: String) {
      webTestClient.get()
        .uri(uri, -1)
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    internal fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`(uri: String) {
      webTestClient.get()
        .uri(uri, -1)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @ParameterizedTest
    @MethodSource("secureEndpoints")
    internal fun `returns success if in user caseload`(uri: String) {
      webTestClient.get()
        .uri(uri, -1)
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }
  }
}
