package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class OffenderFinePaymentResourceTest : ResourceTest() {
  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/offender-fine-payment/booking/-1")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if client does not have override role`() {
    webTestClient.get().uri("/api/offender-fine-payment/booking/-1")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if user has no caseloads`() {
    webTestClient.get().uri("/api/offender-fine-payment/booking/-1")
      .headers(setAuthorisation("RO_USER", listOf())).exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("User not authorised to access booking with id -1.")
  }

  @Test
  fun `returns 403 if not in user caseload`() {
    webTestClient.get().uri("/api/offender-fine-payment/booking/-1")
      .headers(setAuthorisation("WAI_USER", listOf(""))).exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("User not authorised to access booking with id -1.")
  }

  @Test
  fun `returns success if in user caseload`() {
    webTestClient.get().uri("/api/offender-fine-payment/booking/-1")
      .headers(setAuthorisation(listOf())).exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("length()").isEqualTo(1)
  }

  @Test
  fun `returns success if offender is OUT with multiple fines`() {
    webTestClient.get().uri("/api/offender-fine-payment/booking/-20")
      .headers(setAuthorisation(listOf("INACTIVE_BOOKINGS"))).exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("length()").isEqualTo(3)
      .jsonPath("[*].sequence").value<List<Int>> { Assertions.assertThat(it).containsExactlyInAnyOrder(1, 2, 3) }
  }
}
