package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

@DisplayName("GET /api/adjustments/{bookingId}/sentence-and-booking")
class AdjustmentResourceTest : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/adjustments/$BOOKING_ID/sentence-and-booking")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `returns 403 when client does not have override role`() {
    webTestClient.get().uri("/api/adjustments/$BOOKING_ID/sentence-and-booking")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
    webTestClient.get().uri("/api/adjustments/$BOOKING_ID/sentence-and-booking")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 when user does not have offender in caseload`() {
    webTestClient.get().uri("/api/adjustments/$BOOKING_ID/sentence-and-booking")
      .headers(setAuthorisation("WAI_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -6 not found.")
  }

  @Test
  fun returnsExpectedValue() {
    val token = authTokenHelper.getToken(AuthToken.CRD_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/adjustments/$BOOKING_ID/sentence-and-booking",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, 200, "booking-and-sentence-adjustments.json")
  }

  companion object {
    private const val BOOKING_ID: Long = -6
  }
}
