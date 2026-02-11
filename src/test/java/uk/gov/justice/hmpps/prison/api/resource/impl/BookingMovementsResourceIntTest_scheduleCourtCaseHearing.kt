@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper

class BookingMovementsResourceIntTest_scheduleCourtCaseHearing : ResourceTest() {
  @Test
  fun schedules_court_case_hearing() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
        "comments" to "some comments",
      ),
    )

    val response: ResponseEntity<CourtHearing> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      object : ParameterizedTypeReference<CourtHearing>() {
      },
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.CREATED)
    assertThat(response.getBody()!!.id).isNotNull()
    assertThat(response.getBody()!!.dateTime).isEqualTo("2030-03-11T14:00:00")
    assertThat(response.getBody()!!.location.isActive).isEqualTo(true)
    assertThat(response.getBody()!!.location.agencyId).isEqualTo("COURT1")
    assertThat(response.getBody()!!.location.agencyType).isEqualTo("CRT")
    assertThat(response.getBody()!!.location.description).isEqualTo("Court 1")
  }

  @Test
  fun schedules_court_case_hearing_fails_when_no_matching_booking() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/9999999/court-cases/-2/prison-to-court-hearings",
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
  fun schedules_court_case_hearing_fails_when_no_matching_prison() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "PRISON",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
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
  fun schedules_court_case_hearing_fails_when_no_matching_court() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Court with id COURT not found.")
        .developerMessage("Court with id COURT not found.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_no_matching_case_id() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/8888888/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Court case with id 8888888 not found.")
        .developerMessage("Court case with id 8888888 not found.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_unauthorised() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(403)
        .userMessage("Access Denied")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_prison_not_supplied() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Field: fromPrisonLocation - The from prison location must be provided.")
        .developerMessage("Field: fromPrisonLocation - The from prison location must be provided.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_court_not_supplied() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Field: toCourtLocation - The court location to be moved to must be provided.")
        .developerMessage("Field: toCourtLocation - The court location to be moved to must be provided.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_date_not_supplied() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT1",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Field: courtHearingDateTime - The future court hearing date time must be provided.")
        .developerMessage("Field: courtHearingDateTime - The future court hearing date time must be provided.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_prison_longer_than_6_chars() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "PRISONx",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Field: fromPrisonLocation - From location must be a maximum of 6 characters.")
        .developerMessage("Field: fromPrisonLocation - From location must be a maximum of 6 characters.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_court_longer_than_6_chars() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "PRISON",
        "toCourtLocation" to "COURT1x",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Field: toCourtLocation - To location must be a maximum of 6 characters.")
        .developerMessage("Field: toCourtLocation - To location must be a maximum of 6 characters.")
        .build(),
    )
  }

  @Test
  fun schedules_court_case_hearing_fails_when_comments_longer_than_240_chars() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)

    val request = createHttpEntity(
      token,
      mapOf(
        "fromPrisonLocation" to "LEI",
        "toCourtLocation" to "COURT1",
        "courtHearingDateTime" to "2030-03-11T14:00:00.000Z",
        "comments" to StringUtils.repeat("a", 241),
      ),
    )

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
      HttpMethod.POST,
      request,
      ErrorResponse::class.java,
    )

    assertThat(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Field: comments - Comment text must be a maximum of 240 characters.")
        .developerMessage("Field: comments - Comment text must be a maximum of 240 characters.")
        .build(),
    )
  }
}
