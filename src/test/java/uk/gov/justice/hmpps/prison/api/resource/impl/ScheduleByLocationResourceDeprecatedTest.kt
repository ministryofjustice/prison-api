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

class ScheduleByLocationResourceDeprecatedTest : ResourceTest() {

  private val locationIdsNoSchedules: List<Long> = listOf(108582L, 108583L)

  private val locationIdsWithSchedules: List<Long> = listOf(
    -3L, -12L, -1101L, -1002L, -1003L, -1004L, -1005L, -1006L, -1007L, -1008L, -4L,
    -5L, -6L, -7L, -8L, -9L, -10L, -11L, -33L,
  )

  @Nested
  @DisplayName("POST /api/schedules/{agencyId}/events-by-location-id")
  inner class EventsByLocationIds {
    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.post()
        .uri("/api/schedules/RNI/events-by-location-ids")
        .headers(setClientAuthorisation(listOf()))
        .bodyValue(locationIdsNoSchedules)
        .exchange()
        .expectStatus().isForbidden
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
}
