@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GET /api/bookings/offenderNo/{offenderNo}/image/data")
class BookingResourceIntTest_getImageData : ResourceTest() {

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if does not have override role`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 403 when has SYSTEM_USER override role`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has VIEW_PRISONER_DATA override role`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 if user has no caseloads`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .headers(setAuthorisation("RO_USER", listOf()))
      .exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("User not authorised to access booking with id -1.")
  }

  @Test
  fun `returns 403 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .headers(setAuthorisation("WAI_USER", listOf()))
      .exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("User not authorised to access booking with id -1.")
  }

  @Test
  fun `returns 404 if offender not found`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A9999ZZ/image/data")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A9999ZZ] not found.")
  }

  @Test
  fun `should return success if in user caseload`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AA/image/data")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `should return not found if image does not exist`() {
    webTestClient.get().uri("/api/bookings/offenderNo/A1234AO/image/data")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().isEmpty
  }
}
