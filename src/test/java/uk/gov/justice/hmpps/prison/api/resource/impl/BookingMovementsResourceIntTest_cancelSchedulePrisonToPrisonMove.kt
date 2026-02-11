@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper

class BookingMovementsResourceIntTest_cancelSchedulePrisonToPrisonMove : ResourceTest() {
  private lateinit var token: String

  @BeforeEach
  fun setup() {
    token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.PRISON_MOVE_MAINTAINER)
  }

  @Test
  fun cancels_prison_to_prison_move() {
    val request = createHttpEntity(token, mapOf("reasonCode" to "ADMI"))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-3/prison-to-prison/-26/cancel",
      HttpMethod.PUT,
      request,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat<HttpStatusCode?>(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun cancels_prison_to_prison_move_fails_for_unknown_cancellation_reason() {
    val request = createHttpEntity(token, mapOf("reasonCode" to "ADMIX"))

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-3/prison-to-prison/-27/cancel",
      HttpMethod.PUT,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Cancellation reason ADMIX not found.")
        .developerMessage("Cancellation reason ADMIX not found.")
        .build(),
    )
  }

  @Test
  fun cancel_prison_to_prison_move_fails_when_no_matching_booking() {
    val request = createHttpEntity(token, mapOf("reasonCode" to "ADMI"))

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-99999/prison-to-prison/-26/cancel",
      HttpMethod.PUT,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Offender booking with id -99999 not found.")
        .developerMessage("Offender booking with id -99999 not found.")
        .build(),
    )
  }

  @Test
  fun cancel_prison_to_prison_move_fails_when_event_booking_does_not_match_booking() {
    val request = createHttpEntity(token, mapOf("reasonCode" to "ADMI"))

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison/-26/cancel",
      HttpMethod.PUT,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Booking with id -1 not associated with the supplied move id -26.")
        .developerMessage("Booking with id -1 not associated with the supplied move id -26.")
        .build(),
    )
  }

  @Test
  fun schedule_prison_to_prison_fails_when_from_prison_when_unauthorised() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)

    val request = createHttpEntity(token, mapOf("reasonCode" to "ADMI"))

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-3/prison-to-prison/-26/cancel",
      HttpMethod.PUT,
      request,
      ErrorResponse::class.java,
    )

    assertThat<HttpStatusCode?>(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(403)
        .userMessage("Access Denied")
        .build(),
    )
  }

  @Test
  fun cancel_prison_to_prison_move_fails_when_no_matching_move() {
    val request = createHttpEntity(token, mapOf("reasonCode" to "ADMI"))

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-3/prison-to-prison/-88888/cancel",
      HttpMethod.PUT,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Scheduled prison move with id -88888 not found.")
        .developerMessage("Scheduled prison move with id -88888 not found.")
        .build(),
    )
  }
}
