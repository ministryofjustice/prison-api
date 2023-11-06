package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Map

class MovementResourceTest : ResourceTest() {
  @Test
  fun testReadTodaysMovementsForbidden() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements?fromDateTime={fromDateTime}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      now().truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
    assertThatStatus(response, 403)
  }

  @Test
  fun testReadTodaysMovements() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val response = testRestTemplate.exchange(
      "/api/movements?fromDateTime={fromDateTime}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      now().truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
    assertThatStatus(response, 200)
    assertThatJson(response.body).isEqualTo("[]")
  }

  @Test
  fun testGetMovementsForOffenders() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val body = String.format("[ \"%s\" ]", "A1179MT")
    val response = testRestTemplate.exchange(
      "/api/movements/offenders?allBookings=true&latestOnly=false",
      POST,
      createHttpEntity(token, body),
      object : ParameterizedTypeReference<String>() {},
    )
    assertThatStatus(response, 200)
    assertThatJson(response.body).isEqualTo("movements_all_bookings.json".readFile())
  }

  @Test
  fun testGetMovementsForDateRange() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val response = testRestTemplate.exchange(
      "/api/movements?fromDateTime={fromDateTime}&movementDate={movementDate}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      LocalDateTime.of(2018, 4, 25, 0, 0, 0).truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      LocalDate.of(2018, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    )
    assertThatStatus(response, 200)
    assertThatJson(response.body).isEqualTo("movements_on_day.json".readFile())
  }

  @Test
  fun testReadRollcountByAgency() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/rollcount/{agencyId}/movements",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
    )
    assertThatStatus(response, 200)
    assertThatJson(response.body).isEqualTo("{\"in\":0,\"out\":0}")
  }

  @Test
  fun testReadTodaysMovementsByAgencyEnRoute() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/{agencyId}/enroute",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
    )
    assertThatStatus(response, 200)
    assertThatJson(response.body).isEqualTo(
      """
      [{
        "offenderNo": "A1183SH",
          "bookingId":-44,
          "dateOfBirth": "1980-01-02",
          "firstName": "SAM",
          "lastName": "HEMP",
          "fromAgency": "BMI",
          "fromAgencyDescription": "Birmingham",
          "toAgency": "LEI",
          "toAgencyDescription": "Leeds",
          "movementType": "TRN",
          "movementTypeDescription": "Transfers",
          "movementReason": "NOTR",
          "movementReasonDescription": "Normal Transfer",
          "directionCode": "OUT",
          "movementTime": "13:00:00",
          "movementDate": "2017-10-12"
        },
        {
          "offenderNo": "A1183AD",
          "bookingId":-45,
          "dateOfBirth": "1980-01-02",
          "firstName": "AMY",
          "lastName": "DENTON",
          "fromAgency": "BMI",
          "fromAgencyDescription": "Birmingham",
          "toAgency": "LEI",
          "toAgencyDescription": "Leeds",
          "movementType": "TRN",
          "movementTypeDescription": "Transfers",
          "movementReason": "NOTR",
          "movementReasonDescription": "Normal Transfer",
          "directionCode": "OUT",
          "movementTime": "15:00:00",
          "movementDate": "2017-10-12"
        }]
    """,
    )
  }

  @Test
  fun testGetRolllcountByAgencyEnroute() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/rollcount/{agencyId}/enroute",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
    )
    assertThatStatus(response, 200)
    assertThatJson(response.body).isEqualTo("2")
  }

  @Nested
  @DisplayName("/api/movements/{agencyId}/in/{isoDate}")
  inner class MovementsIn {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/LEI/in/2019-01-10")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return success when does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/SFI/in/2019-01-10")
        .headers(setAuthorisation(listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/LEI/in/2019-01-10")
        .headers(setAuthorisation(listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("/api/movements/rollcount/{agencyId}/in-reception")
  inner class MovementsInReception {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/rollcount/LEI/in-reception")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return success when does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/rollcount/SFI/in-reception")
        .headers(setAuthorisation(listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/rollcount/LEI/in-reception")
        .headers(setAuthorisation(listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
    }
  }

  @Test
  fun testGetMovementsSince() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDateTime.of(2019, 1, 1, 0, 1),
    )
    assertThatStatus(response, OK.value())
    assertThatJson(response.body)
    assertThatJson(response.body).isEqualTo("movements_since.json".readFile())
  }

  @Test
  fun testGetAllMovementsSince() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}&allMovements=true",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDateTime.of(2019, 10, 1, 0, 0),
    )
    assertThatStatus(response, OK.value())
    assertThatJson(response.body).isEqualTo("movements_since_all.json".readFile())
  }

  @Test
  fun testGetMovementsPagination() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}",
      GET,
      createHttpEntity(token, null, Map.of("Page-Offset", "1", "Page-Limit", "1")),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDateTime.of(2019, 1, 1, 0, 1),
    )
    assertThatStatus(response, OK.value())
    assertThat(response.headers.toSingleValueMap()).contains(
      Map.entry("Page-Limit", "1"),
      Map.entry("Page-Offset", "1"),
      Map.entry("Total-Records", "2"),
    )
    assertThatJson(response.body).isEqualTo("movements_paged.json".readFile())
  }

  @Test
  fun testGetMovementsBetween() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}&toDateTime={toDateTime}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDateTime.of(2019, 4, 1, 0, 1),
      LocalDateTime.of(2019, 6, 1, 0, 1),
    )
    assertThatStatus(response, OK.value())
    assertThatJson(response.body).isEqualTo("movements_between.json".readFile())
  }

  @Test
  fun testGetUpcomingCourtAppearances() {
    val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
    val response = testRestTemplate.exchange(
      "/api/movements/upcomingCourtAppearances",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
    )
    assertThatStatus(response, OK.value())
    assertThatJson(response.body).isEqualTo("movements_upcoming_court.json".readFile())
  }

  @Test
  fun testGetAllMovementsOutForAGivenDate() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val movementsOutOnDayResponse = testRestTemplate.exchange(
      "/api/movements/{agencyId}/out/{isoDate}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDate.of(2012, 7, 16),
    )
    assertThatStatus(movementsOutOnDayResponse, OK.value())
    assertThatJson(movementsOutOnDayResponse.body).isEqualTo("movements_out_on_given_day.json".readFile())
  }

  @Test
  fun testGetAllMovementsOutForAGivenDateAndMovementType() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val temporaryAbsenceMovementOnDayResponse = testRestTemplate.exchange(
      "/api/movements/{agencyId}/out/{isoDate}?movementType={movementType}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDate.of(2012, 7, 16),
      "tap",
    )
    assertThatStatus(temporaryAbsenceMovementOnDayResponse, OK.value())
    assertThat(getBodyAsJsonContent<Any>(temporaryAbsenceMovementOnDayResponse)).isStrictlyEqualToJson("movements_out_on_given_day_by_type.json")
    val noCourtMovementsOnDayResponse = testRestTemplate.exchange(
      "/api/movements/{agencyId}/out/{isoDate}?movementType={movementType}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      "LEI",
      LocalDate.of(2017, 7, 16),
      "CRT",
    )
    assertThatStatus(noCourtMovementsOnDayResponse, OK.value())
    assertThat(noCourtMovementsOnDayResponse.body).isEqualTo("[]")
  }

  @Nested
  inner class ScheduledMovements {
    @Test
    fun courtEvents() {
      val response = getScheduledMovements(true, false, false)
      assertThatStatus(response, OK.value())
      assertThat(getBodyAsJsonContent<Any>(response)).isStrictlyEqualToJson("get_court_events.json")
    }

    @Test
    fun releaseEvents() {
      val fromDateTime = LocalDate.of(2018, 4, 23).atStartOfDay()
      val toDateTime = LocalDate.of(2018, 4, 23).atTime(20, 10)
      val response = getScheduledMovements(false, true, false, fromDateTime, toDateTime)
      assertThatStatus(response, OK.value())
      assertThatJson(response.body).isEqualTo("get_release_events.json".readFile())
    }

    @Test
    fun transferEvents() {
      val response = getScheduledMovements(false, false, true)
      assertThatStatus(response, OK.value())
      assertThat(getBodyAsJsonContent<Any>(response)).isStrictlyEqualToJson("get_transfer_events.json")
    }

    private fun getScheduledMovements(courtEvents: Boolean, releaseEvents: Boolean, transferEvents: Boolean): ResponseEntity<String> {
      val fromDateTime = LocalDate.of(2020, 1, 1).atTime(9, 0)
      val toDateTime = LocalDate.of(2020, 1, 1).atTime(12, 0)
      return getScheduledMovements(courtEvents, releaseEvents, transferEvents, fromDateTime, toDateTime)
    }

    @Test
    fun testGetOffendersOutOnTemporaryAbsence() {
      val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/movements/agency/{agencyId}/temporary-absences",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
      )
      assertThatStatus(response, OK.value())
      assertThatJson(response.body).isEqualTo("movements_temporary_absence.json".readFile())
    }

    private fun getScheduledMovements(
      courtEvents: Boolean,
      releaseEvents: Boolean,
      transferEvents: Boolean,
      fromDateTime: LocalDateTime,
      toDateTime: LocalDateTime,
    ): ResponseEntity<String> {
      val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
      return testRestTemplate.exchange(
        UriComponentsBuilder
          .fromPath("/api/movements/transfers")
          .queryParam("agencyId", "LEI")
          .queryParam("fromDateTime", fromDateTime)
          .queryParam("toDateTime", toDateTime)
          .queryParam("courtEvents", courtEvents)
          .queryParam("releaseEvents", releaseEvents)
          .queryParam("transferEvents", transferEvents)
          .build()
          .toUriString(),
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
      )
    }
  }
  internal fun String.readFile(): String = this@MovementResourceTest::class.java.getResource(this).readText()
}
