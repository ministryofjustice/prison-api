@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper

class BookingMovementsResourceIntTest_scheduleSchedulePrisonToPrisonMove : ResourceTest() {
  private lateinit var token: String

  @BeforeEach
  fun setup() {
    token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.PRISON_MOVE_MAINTAINER)
  }

  @Test
  fun schedules_prison_to_prison_move() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    )!!.extractingJsonPathNumberValue("$.id").isNotNull()
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    )!!.extractingJsonPathStringValue("$.scheduledMoveDateTime").isEqualTo("2030-03-11T14:00:00")
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    )!!.extractingJsonPathStringValue("$.fromPrisonLocation.agencyId").isEqualTo("LEI")
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    )!!.extractingJsonPathStringValue("$.toPrisonLocation.agencyId").isEqualTo("BXI")
  }

  @Test
  fun schedule_prison_to_prison_move_fails_when_no_matching_booking() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/9999999/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Offender booking with id 9999999 not found.")
        .developerMessage("Offender booking with id 9999999 not found.")
        .build(),
    )
  }

  @Test
  fun schedule_prison_to_prison_fails_when_to_prison_does_not_match_booking() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "BXI",
        "toPrisonLocation" to "LEI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Prison to prison move from prison does not match that of the booking.")
        .developerMessage("Prison to prison move from prison does not match that of the booking.")
        .build(),
    )
  }

  @Test
  fun schedule_prison_to_prison_fails_when_from_prison_not_found() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "PRISON",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Prison with id PRISON not found.")
        .developerMessage("Prison with id PRISON not found.")
        .build(),
    )
  }

  @Test
  fun schedule_prison_to_prison_fails_when_from_prison_when_unauthorised() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(403)
        .userMessage("Access Denied")
        .build(),
    )
  }

  @Test
  fun schedule_prison_to_prison_fails_when_from_prison_not_supplied() {
    val request = createHttpEntity(
      token,
      mapOf(
        "toPrisonLocation" to "BXI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("The from prison location must be provided")
  }

  @Test
  fun schedule_prison_to_prison_fails_when_to_prison_not_supplied() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("The to prison location must be provided")
  }

  @Test
  fun schedule_prison_to_prison_fails_when_escort_type_not_supplied() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXI",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("The escort type must be provided")
  }

  @Test
  fun schedule_prison_to_prison_fails_when_move_date_time_not_supplied() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXI",
        "escortType" to "PECS",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("The move date time must be provided")
  }

  @Test
  fun schedule_prison_to_prison_fails_when_from_prison_longer_than_6_chars() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEIxxxx",
        "toPrisonLocation" to "BXI",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("From prison must be a maximum of 6 characters")
  }

  @Test
  fun schedule_prison_to_prison_fails_when_to_prison_longer_than_6_chars() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXIxxxx",
        "escortType" to "PECS",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("To prison must be a maximum of 6 characters")
  }

  @Test
  fun schedule_prison_to_prison_fails_when_escort_type_longer_than_12_chars() {
    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toPrisonLocation" to "BXI",
        "escortType" to "PECSxxxxxxxxx",
        "scheduledMoveDateTime" to "2030-03-11T14:00:00",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-prison",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    val error = response.getBody()

    assertThat(error!!.status).isEqualTo(400)
    assertThat(error.userMessage).contains("Escort type must be a maximum of 12 characters")
  }
}
