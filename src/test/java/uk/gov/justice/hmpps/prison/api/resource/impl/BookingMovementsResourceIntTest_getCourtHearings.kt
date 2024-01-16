@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse.builder
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER

class BookingMovementsResourceIntTest_getCourtHearings : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/bookings/-3/court-hearings")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if does not have override role`() {
    webTestClient.get().uri("/api/bookings/-3/court-hearings")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns success when client has override role ROLE_COURT_HEARING_MAINTAINER`() {
    webTestClient.get().uri("/api/bookings/-3/court-hearings")
      .headers(setClientAuthorisation(listOf("ROLE_COURT_HEARING_MAINTAINER")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/-3/court-hearings")
      .headers(setAuthorisation("WAI_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun get_court_hearings_for_booking_returns_no_court_hearings() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/bookings/-41/court-hearings",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "get_court_hearings_for_booking_none_found.json")
  }

  @Test
  fun get_court_hearings_for_booking_returns_3_court_hearings_when_no_dates_supplied() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/bookings/-3/court-hearings",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "get_court_hearings_3_for_booking.json")
  }

  @Test
  fun get_court_hearings_for_booking_returns_1_court_hearing_when_from_date_limits_results() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/bookings/-3/court-hearings?fromDate=2019-05-01",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "get_court_hearings_1_for_booking.json")
  }

  @Test
  fun get_court_hearings_for_booking_returns_no_bookings_when_none_in_date_range() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/bookings/-3/court-hearings?toDate=2016-02-18",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "get_court_hearings_for_booking_none_found.json")
  }

  @Test
  fun get_court_hearings_fails_when_no_matching_booking() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/bookings/666/court-hearings",
      GET,
      request,
      ErrorResponse::class.java,
    )

    assertThat(response.body).isEqualTo(
      builder()
        .status(404)
        .userMessage("Offender booking with id 666 not found.")
        .developerMessage("Offender booking with id 666 not found.")
        .build(),
    )
  }

  @Test
  fun get_court_hearings_for_booking_fails_on_invalid_date_range() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/bookings/-1/court-hearings?fromDate=2020-03-23&toDate=2020-03-22",
      GET,
      request,
      ErrorResponse::class.java,
    )

    assertThat(response.body).isEqualTo(
      builder()
        .status(400)
        .userMessage("Invalid date range: toDate is before fromDate.")
        .developerMessage("Invalid date range: toDate is before fromDate.")
        .build(),
    )
  }
}
