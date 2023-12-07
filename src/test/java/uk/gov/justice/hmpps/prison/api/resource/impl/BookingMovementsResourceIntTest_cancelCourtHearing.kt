package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.api.model.CourtHearings
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@ContextConfiguration(classes = [BookingMovementsResourceIntTest_cancelCourtHearing.TestClock::class])
class BookingMovementsResourceIntTest_cancelCourtHearing : ResourceTest() {
  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock {
      return Clock.fixed(Instant.now(), ZoneId.systemDefault())
    }
  }

  @Autowired
  private lateinit var clock: Clock

  private lateinit var token: String

  @BeforeEach
  fun setup() {
    token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.COURT_HEARING_MAINTAINER)
  }

  @Test
  fun cancel_succeeds_of_future_scheduled_court_hearing() {
    val futureScheduledHearing = givenScheduleHearing(token)

    val courtHearingsBeforeCancellation = givenAllCourtHearings(token)

    assertThat(courtHearingsBeforeCancellation).containsOnlyOnce(futureScheduledHearing)

    cancel(futureScheduledHearing)

    val courtHearingsAfterCancellation = givenAllCourtHearings(token)

    assertThat(courtHearingsAfterCancellation).hasSize(courtHearingsBeforeCancellation.size - 1)
    assertThat(courtHearingsAfterCancellation).doesNotContain(futureScheduledHearing)
  }

  @Test
  fun cancel_fails_when_booking_and_hearing_not_found() {
    val response = testRestTemplate.exchange(
      "/api/bookings/-1/court-hearings/-9999999/cancel",
      HttpMethod.DELETE,
      createHttpEntity(token, mapOf<Any, Any>()),
      ErrorResponse::class.java,
    )

    assertThat(response.body).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Court hearing '-9999999' with booking '-1' not found.")
        .developerMessage("Court hearing '-9999999' with booking '-1' not found.")
        .build(),
    )
  }

  @Test
  fun cancel_fails_when_hearing_not_in_future() {
    val response = testRestTemplate.exchange(
      "/api/bookings/-8/court-hearings/-209/cancel",
      HttpMethod.DELETE,
      createHttpEntity(token, mapOf<Any, Any>()),
      ErrorResponse::class.java,
    )

    assertThat(response.body).isEqualTo(
      ErrorResponse.builder()
        .status(400)
        .userMessage("Court hearing '-209' cannot be deleted as its start date/time is in the past.")
        .developerMessage("Court hearing '-209' cannot be deleted as its start date/time is in the past.")
        .build(),
    )
  }

  @Test
  fun cancel_fails_when_not_authorised() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)

    val response = testRestTemplate.exchange(
      "/api/bookings/-8/court-hearings/-209/cancel",
      HttpMethod.DELETE,
      createHttpEntity(token, null),
      ErrorResponse::class.java,
    )

    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    assertThat(response.body).isEqualTo(
      ErrorResponse.builder()
        .status(403)
        .userMessage("Access Denied")
        .build(),
    )
  }

  private fun cancel(hearing: CourtHearing) {
    val response = testRestTemplate.exchange(
      "/api/bookings/-1/court-hearings/" + hearing.id + "/cancel",
      HttpMethod.DELETE,
      createHttpEntity(token, mapOf<Any, Any>()),
      object : ParameterizedTypeReference<Void?>() {
      },
    )

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  private fun givenAllCourtHearings(token: String?): Collection<CourtHearing> {
    val courtHearings = testRestTemplate.exchange(
      "/api/bookings/-1/court-hearings",
      HttpMethod.GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<CourtHearings>() {
      },
    ).body

    assertThat(courtHearings).isNotNull()
    assertThat(courtHearings.hearings).hasSizeGreaterThanOrEqualTo(1)

    return courtHearings.hearings
  }

  private fun givenScheduleHearing(token: String): CourtHearing {
    return testRestTemplate.exchange(
      "/api/bookings/-1/prison-to-court-hearings",
      HttpMethod.POST,
      createHttpEntity(
        token,
        mapOf(
          "fromPrisonLocation" to "LEI",
          "toCourtLocation" to "COURT1",
          "courtHearingDateTime" to LocalDateTime.now(clock).plusDays(1).truncatedTo(ChronoUnit.MINUTES),
        ),
      ),
      object : ParameterizedTypeReference<CourtHearing>() {
      },
    ).body!!
  }
}
