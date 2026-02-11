@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import java.time.LocalDateTime

class BookingMovementsResourceIntTest_createExternalMovement : ResourceTest() {
  @Test
  fun createExternalMovement() {
    val entity = createHttpEntity(
      authTokenHelper.getToken(AuthToken.INACTIVE_BOOKING_USER),
      this.body,
    )

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/movements",
      HttpMethod.POST,
      entity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 201, "create_external_movement.json")
  }

  @Test
  fun returnNotAuthorised_whenTheUserIsMissingTheCorrectRole() {
    val entity = createHttpEntity(
      authTokenHelper.getToken(AuthToken.NORMAL_USER),
      this.body,
    )

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/movements",
      HttpMethod.POST,
      entity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  private val body: Map<String, Any>
    get() = mapOf<String, Any>(
      "bookingId" to -21,
      "fromAgencyId" to "HAZLWD",
      "toAgencyId" to "OUT",
      "movementTime" to LocalDateTime.now(),
      "movementType" to "TRN",
      "movementReason" to "SEC",
      "directionCode" to "OUT",
    )
}
