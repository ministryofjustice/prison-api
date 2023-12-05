package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.Alert
import uk.gov.justice.hmpps.prison.api.model.AlertChanges
import uk.gov.justice.hmpps.prison.api.model.AlertCreated
import uk.gov.justice.hmpps.prison.api.model.BookingActivity
import uk.gov.justice.hmpps.prison.api.model.CreateAlert
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendanceBatch
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS
import java.util.stream.Collectors
import java.util.stream.IntStream

@ContextConfiguration(classes = [BookingResourceIntTest.TestClock::class])
class BookingResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var bookingRepository: BookingRepository

  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock {
      return Clock.fixed(
        LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault(),
      )
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}")
  inner class GetBooking {
    @Test
    fun testGetBooking() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        -2,
      )
      assertThatJsonFileAndStatus(response, 200, "booking_offender_-1.json")
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/v2")
  inner class GetBookingsV2 {
    @Test
    fun testGetBookingsV2ByPrison() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/bookings/v2?prisonId={prisonId}&sort={sort}&image={imageRequired}&legalInfo={legalInfo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        mapOf("prisonId" to "BXI", "sort" to "bookingId,asc", "imageRequired" to "true", "legalInfo" to "true"),
      )
      assertThatJsonFileAndStatus(response, 200, "bxi_caseload_bookings.json")
    }

    @Test
    fun testGetBookingsV2ByPrisonPaginated() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/bookings/v2?prisonId={prisonId}&page={pageNum}&size={pageSize}&image={imageRequired}&legalInfo={legalInfo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        mapOf("prisonId" to "LEI", "pageNum" to "2", "pageSize" to "3", "imageRequired" to "true", "legalInfo" to "true"),
      )
      assertThatJsonFileAndStatus(response, 200, "lei_bookings.json")
    }

    @Test
    fun testGetBookingsV2ByBookingId() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/bookings/v2?bookingId={bookingId1}&bookingId={bookingId2}&bookingId={bookingId3}&image={imageRequired}&legalInfo={legalInfo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId1" to "-1", "bookingId2" to "-2", "bookingId3" to "-3", "imageRequired" to "true", "legalInfo" to "true"),
      )
      assertThatJsonFileAndStatus(response, 200, "bookings_by_id.json")
    }

    @Test
    fun testGetBookingsV2ByOffenderNo() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/bookings/v2?offenderNo={nomsId1}&offenderNo={nomsId2}&image={imageRequired}&legalInfo={legalInfo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        mapOf("nomsId1" to "A1234AA", "nomsId2" to "A1234AB", "imageRequired" to "true", "legalInfo" to "true"),
      )
      assertThatJsonFileAndStatus(response, 200, "bookings_by_nomsId.json")
    }
  }

  @Nested
  @DisplayName("POST /api/bookings/{bookingId}/appointments")
  inner class CreateAppointments {
    private val tomorrowDateTime = now().plusDays(1).withMinute(0).withNano(0)
    private val tomorrow = tomorrowDateTime.toLocalDate()

    private val appointment =
      """
        {
          "appointmentType": "ACTI",
          "locationId": -25,
          "startTime": "$tomorrowDateTime",
          "comment": "A default comment"
          }
      """

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("/api/bookings/{bookingId}/appointments", "A1234AA")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(appointment)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no override role`() {
      webTestClient.post().uri("/api/bookings/{bookingId}/appointments", "-2")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(appointment)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_GLOBAL_APPOINTMENT`() {
      val scheduledEvent = webTestClient.post().uri("/api/bookings/{bookingId}/appointments", "-2")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_APPOINTMENT")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(appointment)
        .exchange()
        .expectStatus().isCreated
        .expectBody(ScheduledEvent::class.java).returnResult().responseBody!!

      bookingRepository.deleteBookingAppointment(scheduledEvent.eventId)
    }

    @Test
    fun `returns 400 when user does not have offender in caseload`() {
      bookingRepository.getBookingAppointments(-2, tomorrow, tomorrow, "startTime", Order.ASC)

      webTestClient.post().uri("/api/bookings/{bookingId}/appointments", "-2")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(appointment)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -2 not found.")
    }

    @Test
    fun `returns 400 when user does not have offender in casedload`() {
      bookingRepository.getBookingAppointments(-2, tomorrow, tomorrow, "startTime", Order.ASC)

      webTestClient.post().uri("/api/bookings/{bookingId}/appointments", "-2")
        .headers(setAuthorisation("WAI_USER", listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(appointment)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -2 not found.")
    }

    @Test
    fun `returns success when user has offender in caseload`() {
      val scheduledEvent = webTestClient.post().uri("/api/bookings/{bookingId}/appointments", "-2")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(appointment)
        .exchange()
        .expectStatus().isCreated
        .expectBody(ScheduledEvent::class.java).returnResult().responseBody!!

      bookingRepository.deleteBookingAppointment(scheduledEvent.eventId)
    }
  }

  @Nested
  @DisplayName("PUT /api/bookings/{bookingId}/activities/{activityId}/attendance")
  inner class UpdateAttendance {
    @Test
    fun testThatUpdateAttendanceIsLockedDown_WhenPayRoleIsMissing() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val body = mapOf("eventOutcome" to "ATT", "performance" to "STANDARD")
      val httpEntity = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/activities/{activityId}/attendance",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        -2,
        -11,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun testUpdateAttendance_WithTheValidRole() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = mapOf("eventOutcome" to "ATT", "performance" to "STANDARD")
      val httpEntity = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/activities/{activityId}/attendance",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        -2,
        -11,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun testUpdateAttendance_WithLockTimeout() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = mapOf("eventOutcome" to "ATT", "performance" to "STANDARD")
      val httpEntity = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/activities/{activityId}/attendance?lockTimeout=true",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        -2,
        -11,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun testUpdateAttendance_WithInvalidBookingId() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = mapOf("eventOutcome" to "ATT", "performance" to "STANDARD")
      val request = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/activities/{activityId}/attendance",
        PUT,
        request,
        ErrorResponse::class.java,
        0,
        -11,
      )
      assertThat(response.body).isEqualTo(
        ErrorResponse.builder()
          .status(404)
          .userMessage("Resource with id [0] not found.")
          .developerMessage("Resource with id [0] not found.")
          .build(),
      )
    }

    @Test
    fun testUpdateAttendance_WithInvalidBookingIdAndLockTimeout() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = mapOf("eventOutcome" to "ATT", "performance" to "STANDARD")
      val request = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/activities/{activityId}/attendance?lockTimeout=true",
        PUT,
        request,
        ErrorResponse::class.java,
        0,
        -11,
      )
      assertThat(response.body).isEqualTo(
        ErrorResponse.builder()
          .status(404)
          .userMessage("Resource with id [0] not found.")
          .developerMessage("Resource with id [0] not found.")
          .build(),
      )
    }

    @Test
    fun testUpdateAttendance_WithInvalidEventIdAndLockTimeout() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = mapOf("eventOutcome" to "ATT", "performance" to "STANDARD")
      val request = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/activities/{activityId}/attendance?lockTimeout=true",
        PUT,
        request,
        ErrorResponse::class.java,
        -2,
        999,
      )
      assertThat(response.body).isEqualTo(
        ErrorResponse.builder()
          .status(404)
          .userMessage("Activity with booking Id -2 and activityId 999 not found")
          .developerMessage("Activity with booking Id -2 and activityId 999 not found")
          .build(),
      )
    }

    @Test
    fun testUpdateAttendance_WithMultipleBookingIds() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = UpdateAttendanceBatch
        .builder()
        .eventOutcome("ATT")
        .performance("STANDARD")
        .bookingActivities(setOf(BookingActivity.builder().activityId(-11L).bookingId(-2L).build()))
        .build()
      val httpEntity = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/activities/attendance",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun testUpdateAttendance_WithMultipleBookingIds_invalidEvent() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = UpdateAttendanceBatch
        .builder()
        .eventOutcome("ATT")
        .performance("STANDARD")
        .bookingActivities(setOf(BookingActivity.builder().activityId(999L).bookingId(-2L).build()))
        .build()
      val httpEntity = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/activities/attendance",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(response.body).contains("Activity with booking Id -2 and activityId 999 not found")
    }

    @Test
    fun testUpdateAttendance_WithMultipleBookingIds_invalidBooking() {
      val token = authTokenHelper.getToken(AuthToken.PAY)
      val body = UpdateAttendanceBatch
        .builder()
        .eventOutcome("ATT")
        .performance("STANDARD")
        .bookingActivities(setOf(BookingActivity.builder().activityId(-11L).bookingId(999L).build()))
        .build()
      val httpEntity = createHttpEntity(token, body)
      val response = testRestTemplate.exchange(
        "/api/bookings/activities/attendance",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(response.body).contains("Activity with booking Id 999 and activityId -11 not found")
    }
  }

  @Nested
  inner class Alerts {
    @Test
    fun testCreateNewAlert_UnAuthorised() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val body = CreateAlert.builder().alertCode("X").alertType("XX").comment("XXX")
        .alertDate(LocalDate.now()).build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<ErrorResponse?>() {},
        -10L,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun testUpdateAlert_UnAuthorised() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val body = AlertChanges.builder().expiryDate(LocalDate.now()).build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert/{alertSeq}",
        PUT,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<ErrorResponse?>() {},
        -1L,
        4,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun testUpdateAlert() {
      val token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT)
      val createdAlert = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(
          token,
          CreateAlert.builder()
            .alertType("L")
            .alertCode("LPQAA")
            .comment("XXX")
            .alertDate(LocalDate.now())
            .build(),
        ),
        object : ParameterizedTypeReference<Alert>() {},
        -14L,
      ).body
      val body = AlertChanges.builder().expiryDate(LocalDate.now()).build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert/{alertSeq}",
        PUT,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<AlertCreated?>() {},
        -14L,
        createdAlert!!.alertId,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun testUpdateAlertWithLockTimeout() {
      val token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT)
      val createdAlert = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(
          token,
          CreateAlert.builder()
            .alertType("L")
            .alertCode("LPQAA")
            .comment("XXX")
            .alertDate(LocalDate.now())
            .build(),
        ),
        object : ParameterizedTypeReference<Alert>() {},
        -15L,
      ).body
      val body = AlertChanges.builder().comment("Test comment").build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert/{alertSeq}?lockTimeout=true",
        PUT,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<AlertCreated?>() {},
        -15L,
        createdAlert!!.alertId,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun testUpdateAlert_CommentTextOnly() {
      val token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT)
      val createdAlert = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(
          token,
          CreateAlert.builder()
            .alertType("L")
            .alertCode("LPQAA")
            .comment("XXX")
            .alertDate(LocalDate.now())
            .build(),
        ),
        object : ParameterizedTypeReference<Alert>() {},
        -14L,
      ).body
      val body = AlertChanges.builder().comment("New comment").build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert/{alertSeq}",
        PUT,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<AlertCreated?>() {},
        -14L,
        createdAlert!!.alertId,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun testCreateNewAlert_BadRequest() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val body = CreateAlert.builder().build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<ErrorResponse?>() {},
        -10L,
      )
      val validationMessages = response.body!!.userMessage
      assertThat(validationMessages).contains("alertType")
      assertThat(validationMessages).contains("alertCode")
      assertThat(validationMessages).contains("comment")
      assertThat(validationMessages).contains("alertDate")
      assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun testCreateNewAlert_MaximumLengths() {
      val token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT)
      val largeText = IntStream.range(1, 1002).mapToObj { _: Int -> "A" }.collect(Collectors.joining(""))
      val body = CreateAlert.builder()
        .alertCode(largeText.substring(0, 13))
        .alertType(largeText.substring(0, 13))
        .comment(largeText)
        .alertDate(LocalDate.now()).build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<ErrorResponse?>() {},
        -10L,
      )
      val validationMessages = response.body!!.userMessage
      assertThat(validationMessages).contains("alertType")
      assertThat(validationMessages).contains("alertCode")
      assertThat(validationMessages).contains("comment")
      assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun testCreateNewAlert() {
      val token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT)
      val body = CreateAlert.builder().alertType("L").alertCode("LPQAA").comment("comments")
        .alertDate(LocalDate.now()).build()
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alert",
        POST,
        createHttpEntity(token, body),
        object : ParameterizedTypeReference<AlertCreated?>() {},
        -10L,
      )
      assertThat(response.body!!.alertId).isGreaterThan(1)
      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }
  }

  @Test
  fun testGetBasicInmateDetailsForOffendersActiveOnlyFalse() {
    val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
    val body = listOf("Z0020ZZ")
    val response: ResponseEntity<List<InmateBasicDetails>> = testRestTemplate.exchange(
      "/api/bookings/offenders?activeOnly=false",
      POST,
      createHttpEntity(token, body),
      object : ParameterizedTypeReference<List<InmateBasicDetails>>() {},
    )
    assertThat(response.body!![0].bookingId).isEqualTo(-20)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun fullOffenderInformation() {
    val response = testRestTemplate.exchange(
      "/api/bookings/{bookingId}?extraInfo=true",
      GET,
      createHttpEntity(NORMAL_USER, null),
      String::class.java,
      "-7",
    )
    assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json")
  }

  @Nested
  @DisplayName("GET /api/bookings/offenderNo/{offenderNo}")
  inner class OffenderDetails {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", "A1234AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", "A1234AA")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when has ROLE_SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("profileInformation").isNotEmpty
        .jsonPath("physicalAttributes").isNotEmpty
        .jsonPath("physicalCharacteristics").isNotEmpty
        .jsonPath("physicalMarks").isNotEmpty
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", "A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("profileInformation").isNotEmpty
        .jsonPath("physicalAttributes").isNotEmpty
        .jsonPath("physicalCharacteristics").isNotEmpty
        .jsonPath("physicalMarks").isNotEmpty
    }

    @Test
    fun fullOffenderInformation_byOffenderNo() {
      val response = testRestTemplate.exchange(
        "/api/bookings/offenderNo/{offenderNo}?extraInfo=true",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        "A1234AG",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json")
    }

    @Test
    fun fullOffenderInformation_byOffenderNoAlt() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        "A1234AG",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json")
    }

    @Test
    fun fullOffenderInformationNoCSRA_byOffenderNo() {
      val response = testRestTemplate.exchange(
        "/api/bookings/offenderNo/{offenderNo}?extraInfo=true",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        "A1184MA",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_extra_info_no_csra.json")
    }

    @Test
    fun fullOffenderInformationWithCSRA_byOffenderNo() {
      val response = testRestTemplate.exchange(
        "/api/bookings/offenderNo/{offenderNo}?fullInfo=true&extraInfo=true&csraSummary=true",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        "A1184MA",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_extra_info_with_csra.json")
    }

    @Test
    fun fullOffenderInformationPersonalCare_byOffenderNo() {
      val response = testRestTemplate.exchange(
        "/api/bookings/offenderNo/{offenderNo}?extraInfo=true",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        "A1234AA",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_personal_care.json")
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/mainOffence")
  inner class GetMainOffence {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-1/mainOffence")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-1/mainOffence")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/mainOffence")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-1/mainOffence")
        .headers(setAuthorisation("WAI_USER", listOf())).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -1 not found.")
    }

    @Test
    fun `returns success for single offence`() {
      webTestClient.get().uri("/api/bookings/-1/mainOffence")
        .headers(setAuthorisation(listOf(""))).exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(1)
        .jsonPath("[0].bookingId").isEqualTo("-1")
        .jsonPath("[0].offenceDescription").isEqualTo("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)")
        .jsonPath("[0].offenceCode").isEqualTo("RV98011")
        .jsonPath("[0].statuteCode").isEqualTo("RV98")
    }

    @Test
    fun `returns success for multiple offences`() {
      webTestClient.get().uri("/api/bookings/-7/mainOffence")
        .headers(setAuthorisation(listOf(""))).exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(2)
        .jsonPath("[*].bookingId").value<List<Int>> { assertThat(it).containsOnly(-7) }
        .jsonPath("[0].offenceDescription").isEqualTo("Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury")
        .jsonPath("$[*].offenceCode").value<List<String>> { assertThat(it).containsExactlyElementsOf(listOf("RC86355", "RC86360")) }
        .jsonPath("$[*].statuteCode").value<List<String>> { assertThat(it).containsOnly("RC86") }
    }

    @Test
    fun `returns success for no offences`() {
      webTestClient.get().uri("/api/bookings/-9/mainOffence")
        .headers(setAuthorisation(listOf(""))).exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(0)
    }
  }

  @Nested
  @DisplayName("POST /api/bookings/mainOffence")
  inner class MainOffence {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("/api/bookings/mainOffence")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[-1, -7]")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have authorized role`() {
      webTestClient.post().uri("/api/bookings/mainOffence")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[-1, -7]")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun offenceHistory_post() {
      webTestClient.post().uri("/api/bookings/mainOffence")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue("[-1, -7]")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(3)
        .jsonPath("[*].bookingId").value<List<Int>> { assertThat(it).containsOnly(-1, -7) }
        .jsonPath("[0].offenceDescription").isEqualTo("Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.")
        .jsonPath("[1].offenceDescription").isEqualTo("Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury")
        .jsonPath("[2].offenceDescription").isEqualTo("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)")
        .jsonPath("$[*].offenceCode").value<List<String>> { assertThat(it).containsExactlyElementsOf(listOf("RC86360", "RC86355", "RV98011")) }
        .jsonPath("$[*].statuteCode").value<List<String>> { assertThat(it).containsOnly("RC86", "RV98") }
    }

    @Test
    fun offenceHistory_post_no_offences() {
      webTestClient.post().uri("/api/bookings/mainOffence")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue("[-98, -99]")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(0)
    }
  }

  @Test
  fun offenceHistory() {
    val response = testRestTemplate.exchange(
      "/api/bookings/offenderNo/{offenderNo}/offenceHistory",
      GET,
      createHttpEntity(VIEW_PRISONER_DATA, null),
      String::class.java,
      "A1234AG",
    )
    assertThatJsonFileAndStatus(response, 200, "offender_main_offences.json")
  }

  @Test
  fun offenceHistoryIncludeOffenderWithoutConviction() {
    val response = testRestTemplate.exchange(
      "/api/bookings/offenderNo/{offenderNo}/offenceHistory?convictionsOnly=false",
      GET,
      createHttpEntity(VIEW_PRISONER_DATA, null),
      String::class.java,
      "A1234AB",
    )
    assertThatJsonFileAndStatus(response, 200, "offender_offence_history_A12234AB_include_non_convictions.json")
  }

  @Test
  fun offenceHistoryForBookings() {
    val token = validToken(listOf("ROLE_VIEW_PRISONER_DATA"))
    val httpEntity = createHttpEntity(token, listOf(-59, -7, -3))
    val response = testRestTemplate.exchange(
      "/api/bookings/offence-history",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, 200, "offence-history-by-bookingids.json")
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/secondary-languages")
  inner class SecondaryLanguages {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/secondary-languages")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-3/secondary-languages")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(3)
        .jsonPath("[*].bookingId").value<List<Int>> { assertThat(it).containsOnly(-3) }
        .jsonPath("[*].code").value<List<String>> { assertThat(it).containsOnly("ENG", "KUR", "SPA") }
        .jsonPath("[*].description").value<List<String>> { assertThat(it).containsOnly("English", "Kurdish", "Spanish; Castilian") }
        .jsonPath("[0].canRead").isEqualTo(true)
        .jsonPath("[0].canWrite").isEqualTo(true)
        .jsonPath("[0].canSpeak").isEqualTo(true)
        .jsonPath("[1].canRead").isEqualTo(false)
        .jsonPath("[1].canWrite").isEqualTo(false)
        .jsonPath("[1].canSpeak").isEqualTo(true)
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/visits/next")
  inner class VisitsNext {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-3/visits/next")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 as endpoint does not have override role`() {
      webTestClient.get().uri("/api/bookings/-3/visits/next")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/bookings/-3/visits/next")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-3/visits/next")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun nextVisit() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/next",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -3L,
      )
      assertThatJsonFileAndStatus(response, 200, "next-visit.json")
    }

    @Test
    fun nextVisit_withVisitors() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/next?withVisitors=true",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -3L,
      )
      assertThatJsonFileAndStatus(response, 200, "next-visit-with-visitors.json")
    }

    @Test
    fun nextVisit_withVisitors_whenNotPresent() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/next?withVisitors=true",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1,
      )
      assertThatStatus(response, 200)
    }

    @Test
    fun nextVisit_whenNotPresent() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/next",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1,
      )
      assertThatStatus(response, 200)
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/assessments")
  inner class Assessments {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 as endpoint does not have override role`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has VIEW_ASSESSMENTS override role`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .headers(setClientAuthorisation(listOf("VIEW_ASSESSMENTS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -6 not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -6 not found.")
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/assessments")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-6/assessments")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(3)
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/activities")
  inner class Activities {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 for client if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/activities")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_SCHEDULES")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when client has override role ROLE_VIEW_SCHEDULES`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_SCHEDULES")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
      verify(telemetryClient).trackEvent(eq("UserUnauthorisedBookingAccess"), any(), isNull())
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/activities")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-3/activities")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/balances")
  inner class Balances {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 for client if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/balances")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
      verify(telemetryClient).trackEvent(eq("UserUnauthorisedBookingAccess"), any(), isNull())
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/balances")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-3/balances")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return balances`() {
      webTestClient.get().uri("/api/bookings/-1/balances")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("cash").isEqualTo("1.24")
        .jsonPath("spends").isEqualTo("2.5")
        .jsonPath("savings").isEqualTo("200.5")
        .jsonPath("currency").isEqualTo("GBP")
    }

    @Test
    fun `should return no finance for offender that has no finance records`() {
      webTestClient.get().uri("/api/bookings/-32/balances")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("cash").isEqualTo("0.0")
        .jsonPath("spends").isEqualTo("0.0")
        .jsonPath("savings").isEqualTo("0.0")
        .jsonPath("currency").isEqualTo("GBP")
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/reasonable-adjustments")
  inner class ReasonableAdjustments {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 200 when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC&type=PEEP")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")
      verify(telemetryClient).trackEvent(eq("UserUnauthorisedBookingAccess"), any(), isNull())
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/reasonable-adjustments?type=WHEELCHR_ACC")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-3/reasonable-adjustments?type=WHEELCHR_ACC")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/property")
  inner class GetProperty {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-6/property")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/bookings/-6/property")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when has ROLE_SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/bookings/-6/property")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-6/property")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-6/property")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -6 not found.")
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/property")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-6/property")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/military-records")
  inner class GetMilitaryRecords {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-6/military-records")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/bookings/-6/military-records")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when has ROLE_SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/bookings/-6/military-records")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-6/military-records")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-6/military-records")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -6 not found.")
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/bookings/-99999/military-records")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/bookings/-6/military-records")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/visits-with-visitors")
  inner class VisitsWithVisitors {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-6/visits-with-visitors")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 as endpoint does not have override role`() {
      webTestClient.get().uri("/api/bookings/-6/visits-with-visitors")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun visitsWithVisitorsWithMissingPageAndSize() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatJsonFileAndStatus(response, 200, "visits_with_visitors.json")
    }

    @Test
    fun visitsWithVisitorsWithPageAndSize() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=5&page=1",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatJsonFileAndStatus(response, 200, "visits_with_visitors_paged.json")
    }

    @Test
    fun visitsWithVisitorsWithPageAndSizeAsMax() {
      val page = Int.MAX_VALUE
      val size = 1
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=$size&page=$page",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 200)
    }

    @Test
    fun visitsWithVisitorsWithPageAsMin() {
      val page = Int.MIN_VALUE
      val size = 20
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=$size&page=$page",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 400)
      assertThat(response.body).contains("Page index must not be less than zero")
    }

    @Test
    fun visitsWithVisitorsWithSizeAsMin() {
      val page = 0
      val size = Int.MIN_VALUE
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=$size&page=$page",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 400)
      assertThat(response.body).contains("Page size must not be less than one")
    }

    @Test
    fun visitsWithVisitorsWithOutOfRangePage() {
      val page = Long.MAX_VALUE
      val size = 20
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=$size&page=$page",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 400)
      assertThat(response.body).contains("For input string: \\\"9223372036854775807\\\"")
    }

    @Test
    fun visitsWithVisitorsWithOutOfRangeSize() {
      val page = 0
      val size = Long.MAX_VALUE
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=$size&page=$page",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 400)
      assertThat(response.body).contains("For input string: \\\"9223372036854775807\\\"")
    }

    @Test
    fun visitsWithVisitorsWithNonNumberPage() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=1&page=123x",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 400)
      assertThat(response.body).contains("For input string: \\\"123x\\\"")
    }

    @Test
    fun visitsWithVisitorsWithNonNumberSize() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?size=1x&page=0",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatStatus(response, 400)
      assertThat(response.body).contains("For input string: \\\"1x\\\"")
    }

    @Test
    fun visitsWithVisitorsFilteredPagination() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?fromDate=2019-07-15&size=5",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -6L,
      )
      assertThatJsonFileAndStatus(response, 200, "visits_with_visitors_filter_paged.json")
    }

    @Test
    fun visitsWithVisitorsWithStatus() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?visitStatus=CANC",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      assertThatJsonFileAndStatus(response, 200, "visits_with_visitors_filtered_by_status_paged.json")
    }

    @Test
    fun visitsWithVisitorsFilteredByPrison() {
      val responseAll = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?prisonId=",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      assertThat(getBodyAsJsonContent<Any>(responseAll)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(15)
      val responseLei = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?prisonId=LEI",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      assertThat(getBodyAsJsonContent<Any>(responseLei)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(13)
      val responseMdi = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?prisonId=MDI",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      assertThat(getBodyAsJsonContent<Any>(responseMdi)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(1)
    }

    @Test
    fun visitsWithVisitorsFilteredByCancellationReason() {
      val responseAll = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?cancellationReason=",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      assertThat(getBodyAsJsonContent<Any>(responseAll)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(15)
      val responseLei = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits-with-visitors?cancellationReason=NSHOW",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      assertThat(getBodyAsJsonContent<Any>(responseLei)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(2)
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/sentenceDetail")
  inner class GetBookingSentenceDetail {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/bookings/-1/sentenceDetail")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("api/bookings/-1/sentenceDetail")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("api/bookings/-1/sentenceDetail")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("api/bookings/-1/sentenceDetail")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/sentenceAdjustments")
  inner class GetBookingSentenceAdjustments {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/bookings/-1/sentenceAdjustments")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("api/bookings/-1/sentenceAdjustments")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("api/bookings/-1/sentenceAdjustments")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("api/bookings/-1/sentenceAdjustments")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/visits/prisons")
  inner class GetBookingVisitsPrisons {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-1/visits/prisons")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-1/visits/prisons")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-1/visits/prisons")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/bookings/-1/visits/prisons")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun success() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/prisons",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[0].prisonId").isEqualTo("LEI")
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[0].prison").isEqualTo("Leeds")
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[1].prisonId").isEqualTo("MDI")
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[1].prison").isEqualTo("Moorland")
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[2].prisonId").isEqualTo("BXI")
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[2].prison").isEqualTo("Brixton")
    }

    @Test
    fun forbidden() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/prisons",
        GET,
        createHttpEntity(createJwt("NO_USER", emptyList()), null),
        String::class.java,
        -1L,
      )
      assertThatStatus(response, 404)
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/visits/summary")
  inner class GetBookingVisitsSummary {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-1/visits/summary")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-1/visits/summary")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-1/visits/summary")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/bookings/-1/visits/summary")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun success_visits() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/summary",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -3L,
      )
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.startDateTime").isEqualTo(
        now()
          .truncatedTo(DAYS)
          .plus(1, DAYS)
          .plus(10, HOURS)
          .format(ISO_DATE_TIME),
      )
      assertThat(bodyAsJsonContent).extractingJsonPathBooleanValue("$.hasVisits").isEqualTo(true)
    }

    @Test
    fun success_nonextvisit() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/summary",
        GET,
        createHttpEntity(NORMAL_USER, null),
        String::class.java,
        -1L,
      )
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(bodyAsJsonContent).extractingJsonPathBooleanValue("$.hasVisits").isEqualTo(true)
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.startDateTime").isBlank()
    }

    @Test
    fun forbidden() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/visits/summary",
        GET,
        createHttpEntity(createJwt("NO_USER", emptyList()), null),
        String::class.java,
        -1L,
      )
      assertThatStatus(response, 404)
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/return-to-custody")
  inner class GetFixedTermRecallDetails {
    @Test
    fun success() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/return-to-custody",
        GET,
        createHttpEntity(VIEW_PRISONER_DATA, null),
        String::class.java,
        -20L,
      )
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.bookingId").isEqualTo(-20)
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.returnToCustodyDate").isEqualTo("2001-01-01")
    }

    @Test
    fun notFound() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/return-to-custody",
        GET,
        createHttpEntity(VIEW_PRISONER_DATA, null),
        String::class.java,
        -9999L,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun testFixedTermRecallDetails_success() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/fixed-term-recall",
        GET,
        createHttpEntity(VIEW_PRISONER_DATA, null),
        String::class.java,
        -20L,
      )
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.bookingId").isEqualTo(-20)
      assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.recallLength").isEqualTo(14)
      assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.returnToCustodyDate").isEqualTo("2001-01-01")
    }

    @Test
    fun testFixedTermRecallDetails_notFound() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/fixed-term-recall",
        GET,
        createHttpEntity(VIEW_PRISONER_DATA, null),
        String::class.java,
        -9999L,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
  }

  @Test
  fun courtEventOutcomes() {
    val response = testRestTemplate.exchange(
      "/api/bookings/court-event-outcomes",
      POST,
      createHttpEntity(VIEW_PRISONER_DATA, setOf(-4)),
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
    assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$[0].eventId").isEqualTo(-204)
    assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$[0].bookingId").isEqualTo(-4)
    assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[0].outcomeReasonCode").isEqualTo("1024")
  }
}
