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
import org.springframework.http.ResponseEntity
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Map.entry

class MovementResourceTest : ResourceTest() {
  @Nested
  @DisplayName("GET api/movements")
  inner class Movements {
    @Test
    fun testReadTodaysMovementsForbidden() {
      val token = authTokenHelper.getToken(NORMAL_USER)
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
      assertThatJson(response.body!!).isEqualTo("[]")
    }

    @Test
    fun testGetMovementsForDateRange() {
      val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/movements?fromDateTime={fromDateTime}&movementDate={movementDate}",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        LocalDateTime.of(2018, 4, 25, 0, 0, 0).truncatedTo(ChronoUnit.DAYS)
          .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        LocalDate.of(2018, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
      )
      assertThatStatus(response, 200)
      assertThatJson(response.body!!).isEqualTo("movements_on_day.json".readFile())
    }
  }

  @Nested
  @DisplayName("POST /api/movements/offenders")
  inner class GetMovementsForOffenders {
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
      assertThatJson(response.body!!).isEqualTo("movements_all_bookings.json".readFile())
    }
  }

  @Nested
  @DisplayName("GET /api/movements/rollcount/{agencyId}")
  inner class GetRollcount {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/rollcount/BMI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/rollcount/BMI")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("/api/movements/rollcount/BMI")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/movements/rollcount/{agencyId}/movements")
  inner class GetMovementRollcount {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/rollcount/BMI/movements")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/rollcount/BMI/movements")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("/api/movements/rollcount/BMI/movements")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testReadRollcountByAgency() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/movements/rollcount/{agencyId}/movements",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
      )
      assertThatStatus(response, 200)
      assertThatJson(response.body!!).isEqualTo("{\"in\":0,\"out\":0}")
    }
  }

  @Nested
  @DisplayName("GET /api/movements/{agencyId}/enroute")
  inner class GetMovementsByAgencyEnRoute {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/movements/LEI/enroute")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/movements/LEI/enroute")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("api/movements/LEI/enroute")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testReadTodaysMovementsByAgencyEnRoute() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/movements/{agencyId}/enroute",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
      )
      assertThatStatus(response, 200)
      assertThatJson(response.body!!).isEqualTo(
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
  }

  @Nested
  @DisplayName("GET /api/movements/rollcount/{agencyId}/enroute")
  inner class RollcountEnroute {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/movements/rollcount/LEI/enroute")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/movements/rollcount/LEI/enroute")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun testGetRollcountByAgencyEnroute() {
      webTestClient.get().uri("/api/movements/rollcount/LEI/enroute")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectBody()
        .jsonPath("$").isEqualTo("2")
    }
  }

  @Nested
  @DisplayName("GET /api/movements/{agencyId}/in/{isoDate}")
  inner class MovementsInByDateOnly {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/LEI/in/2019-01-10")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/BMI/in/2019-01-10")
        .headers(setAuthorisation("WAI_USER", listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when user has no caseloads`() {
      webTestClient.get().uri("/api/movements/BMI/in/2019-01-10")
        .headers(setAuthorisation("RO_USER", listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 404 when agency does not exist`() {
      webTestClient.get().uri("/api/movements/doesnotExist/in/2019-01-10")
        .headers(setAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/LEI/in/2019-01-10")
        .headers(setAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/movements/rollcount/{agencyId}/in-reception")
  inner class MovementsInReception {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/rollcount/LEI/in-reception")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 404 when does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/rollcount/SFI/in-reception")
        .headers(setAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/rollcount/LEI/in-reception")
        .headers(setAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/movements/{agencyId}/in")
  inner class MovementsIn {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/LEI/in?fromDateTime=2019-01-10T10:35:17")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 404 when does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/BMI/in?fromDateTime=2019-01-10T10:35:17")
        .headers(setAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/BMI/in?fromDateTime=2019-01-10T10:35:17")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("/api/movements/BMI/in?fromDateTime=2019-01-10T10:35:17")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/LEI/in?fromDateTime=2019-01-10T10:35:17")
        .headers(setAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testGetMovementsSince() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
        LocalDateTime.of(2019, 1, 1, 0, 1),
      )
      assertThatStatus(response, OK.value())
      assertThatJson(response.body!!).isEqualTo("movements_since.json".readFile())
    }

    @Test
    fun testGetAllMovementsSince() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}&allMovements=true",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
        LocalDateTime.of(2019, 10, 1, 0, 0),
      )
      assertThatStatus(response, OK.value())
      assertThatJson(response.body!!).isEqualTo("movements_since_all.json".readFile())
    }

    @Test
    fun testGetMovementsPagination() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val response = testRestTemplate.exchange(
        "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}",
        GET,
        createHttpEntity(token, null, mapOf("Page-Offset" to "1", "Page-Limit" to "1")),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
        LocalDateTime.of(2019, 1, 1, 0, 1),
      )
      assertThatStatus(response, OK.value())
      assertThat(response.headers.toSingleValueMap()).contains(
        entry("Page-Limit", "1"),
        entry("Page-Offset", "1"),
        entry("Total-Records", "2"),
      )
      assertThatJson(response.body!!).isEqualTo("movements_paged.json".readFile())
    }

    @Test
    fun testGetMovementsBetween() {
      val token = authTokenHelper.getToken(NORMAL_USER)
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
      assertThatJson(response.body!!).isEqualTo("movements_between.json".readFile())
    }
  }

  @Nested
  @DisplayName("GET /api/movements/upcomingCourtAppearances")
  inner class UpcomingCourtAppearances {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/upcomingCourtAppearances")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when not does not have correct role`() {
      webTestClient.get().uri("/api/movements/upcomingCourtAppearances")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has authorised VIEW_COURT_EVENTS role`() {
      webTestClient.get().uri("/api/movements/upcomingCourtAppearances")
        .headers(setClientAuthorisation(listOf("VIEW_COURT_EVENTS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testGetUpcomingCourtAppearances() {
      webTestClient.get().uri("/api/movements/upcomingCourtAppearances")
        .headers(setClientAuthorisation(listOf("VIEW_COURT_EVENTS")))
        .exchange()
        .expectBody()
        .jsonPath("$[0].court").isEqualTo("COURT1")
        .jsonPath("$[0].courtDescription").isEqualTo("Court 1")
        .jsonPath("$[0].startTime").isEqualTo("2040-01-01T12:00:00")
        .jsonPath("$[0].offenderNo").isEqualTo("A1234AH")
        .jsonPath("$[0].eventSubType").isEqualTo("CRT")
        .jsonPath("$[0].eventDescription").isEqualTo("Court Appearance")
        .jsonPath("$[0].hold").isEqualTo(false)
        .jsonPath("$[1].court").isEqualTo("ABDRCT")
        .jsonPath("$[1].courtDescription").isEqualTo("Court 2")
        .jsonPath("$[1].startTime").isEqualTo("2050-01-01T11:00:00")
        .jsonPath("$[1].offenderNo").isEqualTo("A1234AH")
        .jsonPath("$[1].eventSubType").isEqualTo("DC")
        .jsonPath("$[1].eventDescription").isEqualTo("Discharged to Court")
        .jsonPath("$[1].hold").isEqualTo(true)
    }
  }

  @Nested
  @DisplayName("GET /api/movements/{agencyId}/out/{isoDate}")
  inner class MovementsOutByDate {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/LEI/out/2012-07-16")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/LEI/out/2012-07-16")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("/api/movements/LEI/out/2012-07-16")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return 404 when agency does not exist`() {
      webTestClient.get().uri("/api/movements/doesNotNExist/out/2012-07-16")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when user does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/LEI/out/2012-07-16")
        .headers(setAuthorisation("WAI_USER", listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when user has no caseloads`() {
      webTestClient.get().uri("/api/movements/LEI/out/2012-07-16")
        .headers(setAuthorisation("RO_USER", listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun testGetAllMovementsOutForAGivenDate() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val movementsOutOnDayResponse = testRestTemplate.exchange(
        "/api/movements/{agencyId}/out/{isoDate}",
        GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "LEI",
        LocalDate.of(2012, 7, 16),
      )
      assertThatStatus(movementsOutOnDayResponse, OK.value())
      assertThatJson(movementsOutOnDayResponse.body!!).isEqualTo("movements_out_on_given_day.json".readFile())
    }

    @Test
    fun testGetAllMovementsOutForAGivenDateAndMovementType() {
      val token = authTokenHelper.getToken(NORMAL_USER)
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
  }

  @Nested
  @DisplayName("GET /api/movements/agency/{agencyId}/temporary-absences")
  inner class GetOffendersOutOnTemporaryAbsence {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/movements/agency/LEI/temporary-absences")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun testGetOffendersOutOnTemporaryAbsence() {
      webTestClient.get().uri("/api/movements/agency/LEI/temporary-absences")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
      [
  {
    "offenderNo": "Z0025ZZ",
    "firstName": "MATTHEW",
    "lastName": "SMITH",
    "dateOfBirth": "1974-01-01",
    "movementTime": "2021-12-01T00:00:00",
    "toCity": "Birmingham",
    "movementReasonCode": "C3",
    "movementReason": "Funerals And Deaths"
  }
]
       """,
        )
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/agency/LEI/temporary-absences")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("GET /api/movements/transfers")
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
      assertThatJson(response.body!!).isEqualTo("get_release_events.json".readFile())
    }

    @Test
    fun transferEvents() {
      val response = getScheduledMovements(false, false, true)
      assertThatStatus(response, OK.value())
      assertThat(getBodyAsJsonContent<Any>(response)).isStrictlyEqualToJson("get_transfer_events.json")
    }

    private fun getScheduledMovements(
      courtEvents: Boolean,
      releaseEvents: Boolean,
      transferEvents: Boolean,
    ): ResponseEntity<String> {
      val fromDateTime = LocalDate.of(2020, 1, 1).atTime(9, 0)
      val toDateTime = LocalDate.of(2020, 1, 1).atTime(12, 0)
      return getScheduledMovements(courtEvents, releaseEvents, transferEvents, fromDateTime, toDateTime)
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

  @Nested
  @DisplayName("GET /api/movements/livingUnit/{livingUnitId}/currently-out")
  inner class CurrentlyOutLivingUnit {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/livingUnit/1/currently-out")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/livingUnit/1/currently-out")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("/api/movements/livingUnit/-3001/currently-out")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testGetCurrentlyOutLivingUnit() {
      webTestClient.get().uri("/api/movements/livingUnit/-13/currently-out")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
      [{"offenderNo":"Z0024ZZ","bookingId":-24,"dateOfBirth":"1958-01-01","firstName":"Lucius","lastName":"Fox","location":"Landing H/1"},
       {"offenderNo":"Z0025ZZ","bookingId":-25,"dateOfBirth":"1974-01-01","firstName":"Matthew","lastName":"Smith","location":"Landing H/1"}]
        """,
        )
    }
  }

  @Nested
  @DisplayName("GET /api/agency/{agencyId}/currently-out")
  inner class CurrentlyOutAgency {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/agency/LEI/currently-out")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have role`() {
      webTestClient.get().uri("/api/movements/agency/LEI/currently-out")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ESTABLISHMENT_ROLL override role`() {
      webTestClient.get().uri("/api/movements/agency/LEI/currently-out")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testGetCurrentlyOutAgency() {
      webTestClient.get().uri("/api/movements/agency/LEI/currently-out")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
      [{"offenderNo":"Z0024ZZ","bookingId":-24,"dateOfBirth":"1958-01-01","firstName":"Lucius","lastName":"Fox","location":"Landing H/1"},
       {"offenderNo":"Z0025ZZ","bookingId":-25,"dateOfBirth":"1974-01-01","firstName":"Matthew","lastName":"Smith","location":"Landing H/1"}]
         """,
        )
    }
  }

  internal fun String.readFile(): String = this@MovementResourceTest::class.java.getResource(this)!!.readText()
}
