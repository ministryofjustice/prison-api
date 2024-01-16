@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GET /api/bookings/{bookingId}/contacts")
class BookingResourceIntTest_getOffenderContacts : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/bookings/-1/contacts")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `returns 403 when client does not have any override roles`() {
    webTestClient.get().uri("/api/bookings/-1/contacts")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -1.")
  }

  @Test
  fun `returns 200 when client has override role ROLE_GLOBAL_SEARCH`() {
    webTestClient.get().uri("/api/bookings/-1/contacts")
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 200 when client has override role ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.get().uri("/api/bookings/-1/contacts")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/-1/contacts")
      .headers(setAuthorisation("WAI_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
  }

  @Test
  fun `returns 404 if offender does not exist`() {
    webTestClient.get().uri("/api/bookings/-99999/contacts")
      .headers(setAuthorisation("PRISON_API_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
  }

  @Test
  fun `returns 200 if in user caseload`() {
    webTestClient.get().uri("/api/bookings/-1/contacts")
      .headers(setAuthorisation("ITAG_USER", listOf()))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("offender_contacts.json".readFile())
  }
  internal fun String.readFile(): String = this@BookingResourceIntTest_getOffenderContacts::class.java.getResource(this)!!.readText()
}
