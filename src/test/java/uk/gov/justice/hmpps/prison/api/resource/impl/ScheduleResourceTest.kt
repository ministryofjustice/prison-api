package uk.gov.justice.hmpps.prison.api.resource.impl

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.BodyInserters
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
  inner class ActivitiesGET {
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
  @DisplayName("POST /api/schedules/{agencyId}/activities")
  inner class ActivitiesPOST {
    @Test
    fun `returns 403 if does not have role`() {
      webTestClient.post()
        .uri("/api/schedules/LEI/activities")
        .headers(setClientAuthorisation(listOf()))
        .body(BodyInserters.fromValue(listOf("A1234AB")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun activitiesReturned() {
      webTestClient.post()
        .uri("/api/schedules/LEI/activities")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ACTIVITIES")))
        .body(BodyInserters.fromValue(listOf("A1234AB")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(8)
        .jsonPath("$[0].offenderNo").isEqualTo("A1234AB")
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.ScheduleResourceTest#offendersScheduledActivitiesTable")
    fun `Request an offenders scheduled activities for a specific date`(table: ActivitiesRow) {
      webTestClient.post()
        .uri("/api/schedules/LEI/activities?date=${table.date}&timeSlot=${table.timeSlot}")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ACTIVITIES")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(listOf(table.offenderNo)))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].comment").value<List<String>> { assertThat(it).containsExactlyElementsOf(table.eventList) }
    }
  }

  @Nested
  @DisplayName("GET /api/schedules/locations/{locationId}/activities")
  inner class LocationActivities {

    @Test
    fun `returns 403 if does not have role`() {
      webTestClient.get()
        .uri("/api/schedules/locations/999/activities")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun testThatSuspendedActivity_IsReturned() {
      webTestClient.get()
        .uri("/api/schedules/locations/-27/activities?timeSlot=PM&date=1985-01-01&includeSuspended=true")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ACTIVITIES")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
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
      assertThat(schedules).allSatisfy { s: PrisonerSchedule ->
        assertThat(s.startTime.toLocalTime()).isBefore(LocalTime.of(12, 0))
      }
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
      assertThat(schedules).allSatisfy { s: PrisonerSchedule ->
        assertThat(s.startTime.toLocalTime()).isAfterOrEqualTo(LocalTime.of(12, 0))
      }
      assertThat(schedules).allSatisfy { s: PrisonerSchedule ->
        assertThat(s.startTime.toLocalTime()).isBeforeOrEqualTo(LocalTime.of(17, 0))
      }
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
      assertThat(schedules).allSatisfy { s: PrisonerSchedule ->
        assertThat(s.startTime.toLocalTime()).isAfter(LocalTime.of(17, 0))
      }
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
  @DisplayName("GET /api/schedules/{agencyId}/appointments")
  inner class ScheduledAppointmentsGET {
    @Test
    fun `returns 403 if does not have role`() {
      webTestClient.get()
        .uri("/api/schedules/LEI/appointments?date=2017-01-02")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun scheduledAppointmentsReturned() {
      webTestClient.get()
        .uri("/api/schedules/LEI/appointments?date=2017-01-02")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ACTIVITIES")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """[
          {
            "id": -30,
            "offenderNo":"A1234AB",
            "firstName":"GILLIAN",
            "lastName":"ANDERSON",
            "date":"2017-01-02",
            "startTime":"2017-01-02T18:00:00",
            "endTime":"2017-01-02T18:30:00",
            "appointmentTypeDescription":"Medical - Dentist",
            "appointmentTypeCode":"MEDE",
            "locationDescription":"Medical Centre",
            "locationId":-29,
            "createUserId":"SA",
            "agencyId":"LEI"
          },
          {
            "id": -31,
            "offenderNo":"A1234AB",
            "firstName":"GILLIAN",
            "lastName":"ANDERSON",
            "date":"2017-01-02",
            "startTime":"2017-01-02T19:30:00",
            "endTime":"2017-01-02T20:30:00",
            "appointmentTypeDescription":"Medical - Dentist",
            "appointmentTypeCode":"MEDE",
            "locationDescription":"Visiting Room",
            "locationId":-28,
            "createUserId":"SA",
            "agencyId":"LEI"
          },
          {
            "id":-33,
            "offenderNo":"A1234AB",
            "firstName":"GILLIAN",
            "lastName":"ANDERSON",
            "date":"2017-01-02",
            "startTime":"2017-01-02T19:30:00",
            "endTime":"2017-01-02T20:30:00",
            "appointmentTypeDescription":"Medical - Dentist",
            "appointmentTypeCode":"MEDE",
            "locationDescription":"Visiting Room",
            "locationId":-28,
            "createUserId":"SA",
            "agencyId":"LEI"
          }
        ]
          """.trimIndent(),
        )
    }
  }

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/appointments")
  inner class ScheduledAppointmentsPOST {
    @Test
    fun `returns 403 if does not have role`() {
      webTestClient.post()
        .uri("/api/schedules/LEI/appointments")
        .headers(setClientAuthorisation(listOf()))
        .body(BodyInserters.fromValue(listOf("A1234AB")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun scheduledAppointmentsReturned() {
      webTestClient.post()
        .uri("/api/schedules/LEI/appointments")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ACTIVITIES")))
        .body(BodyInserters.fromValue(listOf("A1234AB")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(6)
        .jsonPath("$[0].offenderNo").isEqualTo("A1234AB")
    }

    @ParameterizedTest
    @MethodSource(
      "uk.gov.justice.hmpps.prison.api.resource.impl.ScheduleResourceTest#scheduledAppointmentsTable",
    )
    fun `Request an offenders scheduled appointments for today`(table: AppointmentScheduleRow) {
      webTestClient.post()
        .uri("/api/schedules/LEI/appointments?timeSlot=${table.timeSlot}&date=${LocalDate.now()}")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_ACTIVITIES")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(listOf(table.offenderNo)))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].eventDescription")
        .value<List<String>> { assertThat(it).containsExactlyElementsOf(table.eventDescriptionList) }
        .jsonPath("$[*].eventLocation")
        .value<List<String>> { assertThat(it).containsExactlyElementsOf(table.eventLocationList) }
    }
  }

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/visits")
  inner class ScheduledVisits {
    @Test
    fun `returns 403 if does not have role`() {
      webTestClient.post()
        .uri("/api/schedules/LEI/visits")
        .headers(setClientAuthorisation(listOf()))
        .body(BodyInserters.fromValue(listOf("A1234AB")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun scheduledAppointmentsReturned() {
      webTestClient.post()
        .uri("/api/schedules/LEI/visits")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .body(BodyInserters.fromValue(listOf("A1234AB")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].offenderNo").isEqualTo("A1234AB")
    }

    @ParameterizedTest
    @MethodSource(
      "uk.gov.justice.hmpps.prison.api.resource.impl.ScheduleResourceTest#offendersScheduledVisitsTable",
    )
    fun `Request an offenders scheduled visits for today`(table: VisitsRow) {
      webTestClient.post()
        .uri("/api/schedules/LEI/visits?timeSlot=${table.timeSlot}&date=${LocalDate.now()}")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(listOf(table.offenderNo)))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].comment").value<List<String>> { assertThat(it).containsExactlyElementsOf(table.visitList) }
    }
  }

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/courtEvents")
  inner class CourtEvents {
    @ParameterizedTest
    @MethodSource(
      "uk.gov.justice.hmpps.prison.api.resource.impl.ScheduleResourceTest#courtEventsTable",
    )
    fun `Request scheduled court events for offender list`(table: CourtEventRow) {
      webTestClient.post()
        .uri("/api/schedules/LEI/courtEvents?timeSlot=${table.timeSlot}&date=${table.date}")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(table.offenderNoList))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].eventId").value<List<Int>> { assertThat(it).containsExactlyElementsOf(table.eventList) }
    }
  }

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/activities-by-event-ids")
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
  @DisplayName("GET /api/schedules/{agencyId}/count-activities")
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
    fun `returns 403 if does not have role`() {
      webTestClient.get()
        .uri("/api/schedules/A1234AC/scheduled-transfers")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun testThatScheduledTransfer_IsReturned() {
      webTestClient.get()
        .uri("/api/schedules/A1234AC/scheduled-transfers")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("\$.length()").value<Int> { assertThat(it).isEqualTo(4) }
        .jsonPath("\$[*].firstName").value<List<String>> { assertThat(it).contains("NORMAN") }
        .jsonPath("\$[*].lastName").value<List<String>> { assertThat(it).contains("BATES") }
        .jsonPath("\$[*].eventLocation").value<List<String>> { assertThat(it).contains("Moorland", "Leeds") }
    }
  }

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/externalTransfers")
  inner class ExternalTransfers {
    @Test
    fun `Request an offenders external transfers for a given date`() {
      webTestClient.post()
        .uri("/api/schedules/LEI/externalTransfers?date=${LocalDate.now()}")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(listOf("A1234AC")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].firstName")
        .value<List<String>> { assertThat(it).containsExactlyElementsOf(listOf("NORMAN", "NORMAN")) }
        .jsonPath("$[*].lastName")
        .value<List<String>> { assertThat(it).containsExactlyElementsOf(listOf("BATES", "BATES")) }
        .jsonPath("$[*].eventDescription")
        .value<List<String>> {
          assertThat(it).containsExactlyElementsOf(
            listOf(
              "Compassionate Transfer",
              "Compassionate Transfer",
            ),
          )
        }
    }
  }

  @Nested
  @DisplayName("GET /api/schedules/{agencyId}/locations/{locationId}/usage/{usage}")
  inner class Usage {

    @Test
    fun `location caseload not accessible`() {
      webTestClient.get()
        .uri("/api/schedules/ZZGHI/locations/-28/usage/APP")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource with id [ZZGHI] not found.")
    }

    @Test
    fun `location does not exist`() {
      webTestClient.get()
        .uri("/api/schedules/RNI/locations/-99/usage/APP")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource with id [-99] not found.")
    }

    @Test
    fun `usage not valid`() {
      webTestClient.get()
        .uri("/api/schedules/LEI/locations/-28/usage/INVALID")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Usage not recognised.")
    }

    @Test
    fun `agency does not exist for non system users`() {
      webTestClient.get()
        .uri("/api/schedules/TEST_AGENCY/locations/-4/usage/INVALID")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource with id [TEST_AGENCY] not found.")
    }

    @Test
    fun `no location scheduled events`() {
      webTestClient.get()
        .uri("/api/schedules/RNI/locations/-24/usage/APP")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        // .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isEmpty()
    }

    @ParameterizedTest
    @MethodSource(
      "uk.gov.justice.hmpps.prison.api.resource.impl.ScheduleResourceTest#locationScheduledEventsInOrderTable",
    )
    fun `location scheduled events in order`(table: LocationScheduleRow) {
      webTestClient.get()
        .uri("/api/schedules/LEI/locations/${table.locationId}/usage/${table.usage}?timeSlot=${table.timeSlot}")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        // .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].lastName").value<List<String>> { assertThat(it).containsExactlyElementsOf(table.lastNameList) }
        .jsonPath("$[*].event").value<List<String>> { assertThat(it).containsExactlyElementsOf(table.eventList) }
        .jsonPath("$[*].startTime").value<List<String>> { assertTimesOfDatetimes(table.startTimeList, it) }
    }

    private fun assertTimesOfDatetimes(expected: List<String>, dateTime: List<String>) {
      assertThat(dateTime).containsExactlyElementsOf(expected.map { "${LocalDate.now()}T$it:00" })
    }
  }

  private val locationIdsNoSchedules: List<Long> = listOf(108582L, 108583L)
  private val locationIdsWithSchedules: List<Long> = listOf(
    -3L, -12L, -1101L, -1002L, -1003L, -1004L, -1005L, -1006L, -1007L, -1008L, -4L,
    -5L, -6L, -7L, -8L, -9L, -10L, -11L, -33L,
  )

  private companion object {
    @JvmStatic
    fun locationScheduledEventsInOrderTable(): List<LocationScheduleRow> {
      return listOf(
        LocationScheduleRow(-28, "VISIT", "", listOf("BATES"), listOf("VISIT"), listOf("01:00")),
        LocationScheduleRow(
          -25,
          "VISIT",
          "",
          listOf("BATES", "MATTHEWS"),
          listOf("VISIT", "VISIT"),
          listOf("00:00", "00:00"),
        ),
        LocationScheduleRow(
          -28,
          "APP",
          "",
          listOf("BATES", "MATTHEWS", "MATTHEWS"),
          listOf("EDUC", "EDUC", "EDUC"),
          listOf("04:00", "01:00", "00:00"),
        ),
        LocationScheduleRow(-29, "APP", "", listOf("BATES"), listOf("MEDE"), listOf("03:00")),
        LocationScheduleRow(
          -26,
          "PROG",
          "AM",
          listOf("ANDERSON", "BATES", "CHAPLIN", "MATTHEWS"),
          listOf("EDUC", "EDUC", "EDUC", "EDUC"),
          listOf("00:00", "00:00", "00:00", "00:00"),
        ),
        LocationScheduleRow(
          -26,
          "PROG",
          "PM",
          listOf("ANDERSON", "ANDERSON", "BATES", "BATES", "CHAPLIN", "CHAPLIN", "MATTHEWS", "MATTHEWS"),
          listOf("EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC"),
          listOf("12:00", "13:00", "12:00", "13:00", "12:00", "13:00", "12:00", "13:00"),
        ),
        LocationScheduleRow(
          -26,
          "PROG",
          "",
          listOf(
            "ANDERSON",
            "ANDERSON",
            "ANDERSON",
            "BATES",
            "BATES",
            "BATES",
            "CHAPLIN",
            "CHAPLIN",
            "CHAPLIN",
            "MATTHEWS",
            "MATTHEWS",
            "MATTHEWS",
          ),
          listOf("EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC", "EDUC"),
          listOf(
            "12:00",
            "13:00",
            "00:00",
            "12:00",
            "13:00",
            "00:00",
            "12:00",
            "13:00",
            "00:00",
            "12:00",
            "13:00",
            "00:00",
          ),
        ),
      )
    }

    @JvmStatic
    fun offendersScheduledActivitiesTable() = listOf(
      ActivitiesRow("2017-09-18", "A1234AA", "AM", listOf("Chapel Cleaner")),
      ActivitiesRow("2017-09-11", "A1234AE", "AM", emptyList()),
      ActivitiesRow("2017-09-12", "A1234AE", "PM", listOf("Woodwork")),
    )

    @JvmStatic
    fun offendersScheduledVisitsTable() =
      listOf(
        VisitsRow("A1234AC", "AM", listOf("Social Contact", "Official Visit")),
        VisitsRow("A1234AC", "PM", listOf("Social Contact")),
        VisitsRow("A1234AC", "", listOf("Social Contact", "Social Contact", "Official Visit")),
        VisitsRow("A1234AE", "AM", listOf("Official Visit")),
        VisitsRow("A1234AE", "PM", emptyList()),
      )

    @JvmStatic
    fun scheduledAppointmentsTable() =
      listOf(
        AppointmentScheduleRow(
          "A1234AC",
          "AM",
          listOf("Education", "Medical - Dentist"),
          listOf("Visiting Room", "Medical Centre"),
        ),
        AppointmentScheduleRow(
          "A1234AC",
          "",
          listOf("Education", "Medical - Dentist"),
          listOf("Visiting Room", "Medical Centre"),
        ),
        AppointmentScheduleRow(
          "A1234AE",
          "AM",
          listOf("Education", "Education"),
          listOf("Visiting Room", "Visiting Room"),
        ),
      )

    @JvmStatic
    fun courtEventsTable() =
      listOf(
        CourtEventRow(listOf("A1234AD", "A1234AE", "A1234AF"), "2017-02-13", "ED", listOf(-104, -105, -106)),
        CourtEventRow(listOf("A1234AD", "A1234AE", "A1234AF"), "2017-02-13", "PM", emptyList()),
        CourtEventRow(listOf("A1234AD", "A1234AE", "A1234AF"), "2017-02-13", "", listOf(-104, -105, -106)),
        CourtEventRow(listOf("A1234AD", "A1234AE", "A1234AF"), "2017-02-14", "ED", emptyList()),
        CourtEventRow(listOf("A1234AC"), "2017-10-15", "AM", listOf(-103)),
      )
  }

  data class LocationScheduleRow(
    val locationId: Int,
    val usage: String,
    val timeSlot: String?,
    val lastNameList: List<String>,
    val eventList: List<String>,
    val startTimeList: List<String>,
  )

  data class ActivitiesRow(
    val date: String,
    val offenderNo: String,
    val timeSlot: String?,
    val eventList: List<String>,
  )

  data class VisitsRow(
    val offenderNo: String,
    val timeSlot: String?,
    val visitList: List<String>,
  )

  data class AppointmentScheduleRow(
    val offenderNo: String,
    val timeSlot: String?,
    val eventDescriptionList: List<String>,
    val eventLocationList: List<String>,
  )

  data class CourtEventRow(
    val offenderNoList: List<String>,
    val date: String,
    val timeSlot: String?,
    val eventList: List<Int>,
  )
}
