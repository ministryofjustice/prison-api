package uk.gov.justice.hmpps.prison.api.resource.impl

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import java.time.LocalDate
import java.time.LocalTime

class ScheduleResourceTest : ResourceTest() {
  @Autowired
  private lateinit var gson: Gson

  @Nested
  @DisplayName("GET /api/schedules/{agencyId}/activities-by-date-range")
  inner class ActivitiesByDateRange {
    @Test
    fun testThatScheduleActivitiesByDateRange_ReturnsData() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/activities-by-date-range?timeSlot=PM&fromDate=2017-09-11&toDate=2017-09-12",
        HttpMethod.GET,
        createHttpEntity(token, ""),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val activities = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(activities).isNotEmpty()
    }
  }

  @Nested
  @DisplayName("GET /api/schedules/{agencyId}/suspended-activities-by-date-range")
  inner class SuspendedActivitiesByDateRange {
    @Test
    fun testThatScheduleSuspendedActivitiesByDateRange_ReturnsData() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/suspended-activities-by-date-range?timeSlot=PM&fromDate=1985-01-01",
        HttpMethod.GET,
        createHttpEntity(token, ""),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val activities = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(activities).isNotEmpty()
    }
  }

  @Nested
  @DisplayName("GET /api/schedules/{agencyId}/activities")
  inner class Activities {
    @Test
    fun testThatScheduleActivities_IsReturnForAllActivityLocations() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/activities?timeSlot=PM&date=2017-09-11",
        HttpMethod.GET,
        createHttpEntity(token, ""),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val activities = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(activities).extracting("locationId").contains(-27L, -26L, -26L, -27L, -26L)
    }
  }

  @Nested
  @DisplayName("GET /api/schedules/locations/{locationId}/activities")
  inner class LocationActivities {

    @Test
    fun testThatSuspendedActivity_IsReturned() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/locations/-27/activities?timeSlot=PM&date=1985-01-01&includeSuspended=true",
        HttpMethod.GET,
        createHttpEntity(token, ""),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val activities = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(activities).hasSize(1)
    }
  }

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/events-by-location-id")
  inner class EventsByLocationIds {
    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.post()
        .uri("/api/schedules/RNI/events-by-location-ids")
        .headers(setClientAuthorisation(listOf("")))
        .bodyValue(locationIdsNoSchedules)
        .exchange()
        .expectStatus().isForbidden

      verify(telemetryClient).trackEvent(eq("ClientUnauthorisedAgencyAccess"), any(), isNull())
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_NoLocationGroupScheduleEvents_ReturnsEmptyList() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationIdsNoSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/RNI/events-by-location-ids",
        HttpMethod.POST,
        createHttpEntity(token, locationIds),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val schedules = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(schedules).isEmpty()
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_LocationGroupScheduleEventsInOrder_OffenderSchedulesAreInOrder() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationIdsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-ids",
        HttpMethod.POST,
        createHttpEntity(token, locationIds),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val schedules = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(schedules).extracting("cellLocation").isSorted()
      assertThat(schedules).extracting("cellLocation").containsOnly("LEI-A-1-1", "LEI-A-1-10")
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_AmTimeslot_MorningSchedulesOnly() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationIdsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-ids?timeSlot=AM",
        HttpMethod.POST,
        createHttpEntity(token, locationIds),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val schedules = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(schedules).allSatisfy { s: PrisonerSchedule -> assertThat(s.startTime.toLocalTime()).isBefore(LocalTime.of(12, 0)) }
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_PmTimeslot_SchedulesBetween1200and1700Only() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationIdsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-ids?timeSlot=PM",
        HttpMethod.POST,
        createHttpEntity(token, locationIds),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val schedules = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(schedules).allSatisfy { s: PrisonerSchedule -> assertThat(s.startTime.toLocalTime()).isAfterOrEqualTo(LocalTime.of(12, 0)) }
      assertThat(schedules).allSatisfy { s: PrisonerSchedule -> assertThat(s.startTime.toLocalTime()).isBeforeOrEqualTo(LocalTime.of(17, 0)) }
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_EveningTimeslot_EveningSchedulesOnly() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationIdsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-ids?timeSlot=ED",
        HttpMethod.POST,
        createHttpEntity(token, locationIds),
        object : ParameterizedTypeReference<List<PrisonerSchedule>>() {},
      )
      val schedules = response.body
      assertThat(response.statusCode.value()).isEqualTo(200)
      assertThat(schedules).allSatisfy { s: PrisonerSchedule -> assertThat(s.startTime.toLocalTime()).isAfter(LocalTime.of(17, 0)) }
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_AgencyNotAccessible_ReturnsNotFound() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val notAnAgency = "ZZGHI"
      val locationIds = locationIdsWithSchedules
      val response = testRestTemplate.exchange(
        "/api/schedules/ZZGHI/events-by-location-ids",
        HttpMethod.POST,
        createHttpEntity(token, locationIds),
        ErrorResponse::class.java,
      )
      val error = response.body!!
      assertThat(response.statusCode.value()).isEqualTo(404)
      assertThat(error.userMessage).contains(notAnAgency).contains("not found")
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_NoLocationsPassed_ReturnsBadRequest() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-ids",
        HttpMethod.POST,
        createHttpEntity(token, listOf<Any>()),
        ErrorResponse::class.java,
      )
      val error = response.body!!
      assertThat(response.statusCode.value()).isEqualTo(400)
      assertThat(error.userMessage).contains("must not be empty")
    }
  }

  @Nested
  @DisplayName("GET  /api/schedules/{agencyId}/appointments")
  inner class ScheduledAppointments {
    @Test
    fun scheduledAppointmentsReturned() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationIdsWithSchedules
      val date = LocalDate.of(2017, 1, 2)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/appointments?date={date}",
        HttpMethod.GET,
        createHttpEntity(token, locationIds),
        object : ParameterizedTypeReference<String?>() {},
        date,
      )
      assertThatJsonFileAndStatus(response, 200, "scheduled-appointments-on-date.json")
    }
  }

  @Nested
  @DisplayName("GET  /api/schedules/{agencyId}/activities-by-event-ids")
  inner class ScheduledActivitiesByEventIds {
    @Test
    fun testGetScheduledActivityById() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val eventIds = listOf(-1L, 91234L)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/activities-by-event-ids",
        HttpMethod.POST,
        createHttpEntity(token, eventIds),
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, 200, "scheduled-activities.json")
    }

    @Test
    fun testThatGetScheduledActivitiesById_ReturnsBadRequest() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/activities-by-event-ids",
        HttpMethod.POST,
        createHttpEntity(token, emptyList<Any>()),
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThat(response.statusCode.value()).isEqualTo(400)
    }

    @Test
    fun testThatGetScheduledActivitiesById_ReturnsNotFound_WhenUserNotInAgency() {
      val token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER)
      val eventIds = listOf(-1L, 91234L)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/activities-by-event-ids",
        HttpMethod.POST,
        createHttpEntity(token, eventIds),
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThat(response.statusCode.value()).isEqualTo(404)
    }
  }

  @Nested
  inner class CountActivities {
    @Test
    fun testCountActivitiesMissingTimeslot() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/count-activities?timeSlot=AM&fromDate=2017-09-11&toDate=2017-09-12",
        HttpMethod.POST,
        createHttpEntity(token, null),
        ErrorResponse::class.java,
      )
      assertThat(response.body!!.userMessage).contains("Required request parameter 'timeSlots'")
      assertThat(response.statusCode.value()).isEqualTo(400)
    }

    @Test
    fun testCountActivitiesMissingFromDate() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/count-activities?timeSlot=AM&toDate=",
        HttpMethod.POST,
        createHttpEntity(token, null),
        ErrorResponse::class.java,
      )
      assertThat(response.body!!.userMessage).contains("Required request parameter 'fromDate'")
      assertThat(response.statusCode.value()).isEqualTo(400)
    }

    @Test
    fun testCountActivitiesMissingToDate() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/count-activities?timeSlot=AM&fromDate=2022-02-20&toDate",
        HttpMethod.POST,
        createHttpEntity(token, null),
        ErrorResponse::class.java,
      )
      assertThat(response.body!!.userMessage).contains("Required request parameter 'toDate'")
      assertThat(response.statusCode.value()).isEqualTo(400)
    }

    @Test
    fun testCountActivitiesNoPermissions() {
      val token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/count-activities?timeSlots=AM&fromDate=2017-09-11&toDate=2017-09-12",
        HttpMethod.POST,
        createHttpEntity(token, "{}"),
        ErrorResponse::class.java,
      )
      assertThat(response.body!!.userMessage).contains("Resource with id [LEI] not found")
      assertThat(response.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun testCountActivities() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/schedules/LEI/count-activities?timeSlots=AM&fromDate=2017-09-11&toDate=2017-09-28",
        HttpMethod.POST,
        createHttpEntity(
          token,
          gson.toJson(
            mapOf(
              "-1" to "5", // these will be ignored as -1 is only in the afternoon
              "-6" to "5", // these will be included in the not recorded count
            ),
          ),
        ),
        String::class.java,
      )
      assertThatOKResponseContainsJson(
        response,
        gson.toJson(
          mapOf(
            "total" to 44,
            "suspended" to 8,
            "notRecorded" to 34,
          ),
        ),
      )
    }
  }

  @Nested
  @DisplayName("GET /api/schedules/{prisonerNumber}/scheduled-transfers")
  inner class ScheduledTransfers {

    @Test
    fun testThatScheduledTransfer_IsReturned() {
      webTestClient.get()
        .uri("/api/schedules/A1234AC/scheduled-transfers")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("\$.length()").value<Int> { assertThat(it).isEqualTo(4) }
        .jsonPath("\$[*].firstName").value<List<String>> { assertThat(it).contains("NORMAN") }
        .jsonPath("\$[*].lastName").value<List<String>> { assertThat(it).contains("BATES") }
        .jsonPath("\$[*].eventLocation").value<List<String>> { assertThat(it).contains("Moorland (HMP & YOI)", "HMP LEEDS") }
    }
  }

  private val locationIdsNoSchedules: List<Long> = listOf(108582L, 108583L)
  private val locationIdsWithSchedules: List<Long> = listOf(
    -3L, -12L, -1101L, -1002L, -1003L, -1004L, -1005L, -1006L, -1007L, -1008L, -4L,
    -5L, -6L, -7L, -8L, -9L, -10L, -11L, -33L,
  )
}
