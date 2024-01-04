package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.returnResult

@DisplayName("GET /api/bookings/{booingId}/image/data")
class BookingResourceIntTest_getBookingImageData : ResourceTest() {

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if does not have override role`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 403 when has SYSTEM_USER override role`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has GLOBAL_SEARCH override role`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `should return success when has VIEW_PRISONER_DATA override role`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 if user has no caseloads`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setAuthorisation("RO_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
  }

  @Test
  fun `returns 404 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setAuthorisation("WAI_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
  }

  @Test
  fun `returns 404 if booking not found`() {
    webTestClient.get().uri("/api/bookings/-999999/image/data")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -999999 not found.")
  }

  @Test
  fun `should return not found if image does not exist`() {
    webTestClient.get().uri("/api/bookings/-35/image/data")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("No Image found for booking Id -35")
  }

  @Test
  fun `should return success if in user caseload`() {
    val res = webTestClient.get().uri("/api/bookings/-1/image/data")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isOk
      .returnResult<ByteArray>().responseBody.blockFirst()!!
    assertThat(res).isNotEmpty()
  }

  @Test
  fun `should return success for full image if in user caseload`() {
    webTestClient.get().uri("/api/bookings/-1/image/data?fullSizeImage=true")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isOk
      .returnResult<ByteArray>().responseBody.blockFirst()!!.isNotEmpty()
  }
}
