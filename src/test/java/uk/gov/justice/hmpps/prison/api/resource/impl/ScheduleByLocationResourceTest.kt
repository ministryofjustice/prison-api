package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import java.time.LocalTime

class ScheduleByLocationResourceTest : ResourceTest() {

  private val locationPathsNoSchedules: List<String> = listOf("S-1-001", "S-1-002")

  private val locationPathsWithSchedules: List<String> = listOf(
    "A-1-1", "A-1-2", "A-1-3", "A-1-4", "A-1-5", "A-1-6", "A-1-7", "A-1-8", "A-1-9", "A-1-10", "A-2-1",
    "A-1-1002", "A-1-1003", "A-1-1004", "A-1-1005", "A-1-1006", "A-1-1007", "A-1-1008",
  )

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/events-by-location-path")
  inner class EventsByLocationIds {
    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.post()
        .uri("/api/schedules/RNI/events-by-location-path")
        .headers(setClientAuthorisation(listOf()))
        .bodyValue(locationPathsNoSchedules)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun schedulesAgencyIdActivitiesByLocationId_NoLocationGroupScheduleEvents_ReturnsEmptyList() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val locationIds = locationPathsNoSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/RNI/events-by-location-path",
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
      val locationIds = locationPathsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-path",
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
      val locationIds = locationPathsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-path?timeSlot=AM",
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
      val locationIds = locationPathsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-path?timeSlot=PM",
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
      val locationIds = locationPathsWithSchedules
      val response: ResponseEntity<List<PrisonerSchedule>> = testRestTemplate.exchange(
        "/api/schedules/LEI/events-by-location-path?timeSlot=ED",
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
      val locationIds = locationPathsWithSchedules
      val response = testRestTemplate.exchange(
        "/api/schedules/ZZGHI/events-by-location-path",
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
        "/api/schedules/LEI/events-by-location-path",
        HttpMethod.POST,
        createHttpEntity(token, listOf<Any>()),
        ErrorResponse::class.java,
      )
      val error = response.body!!
      assertThat(response.statusCode.value()).isEqualTo(400)
      assertThat(error.userMessage).contains("must not be empty")
    }
  }
}
