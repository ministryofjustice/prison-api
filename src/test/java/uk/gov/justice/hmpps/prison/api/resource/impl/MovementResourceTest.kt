package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.Movement
import uk.gov.justice.hmpps.prison.api.model.MovementCount
import uk.gov.justice.hmpps.prison.api.model.OffenderIn
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto
import uk.gov.justice.hmpps.prison.api.model.TransferSummary
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.OffenderBookingId
import uk.gov.justice.hmpps.prison.dsl.isAboutNow
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Map.entry

class MovementResourceTest : ResourceTest() {

  @Autowired
  private lateinit var builder: NomisDataBuilder

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

    @Test
    fun `Retrieve a list of recent movements`() {
      val fromDateTime = "2017-02-20T13:56:00"
      val movementDate = "2017-08-16"
      // val response =
      webTestClient.get()
        .uri("/api/movements?fromDateTime={fromDateTime}&movementDate={movementDate}", fromDateTime, movementDate)
        .headers(setClientAuthorisation(listOf("GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
    [
      {
        "offenderNo": "Z0021ZZ",
        "createDateTime": "2017-02-21T00:00:00",
        "fromAgency": "LEI",
        "toAgency": "OUT",
        "movementType": "REL",
        "directionCode": "OUT",
        "movementDate":"2017-08-16",
        "movementTime":"00:00:00"
      }
    ]
       """,
        )
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
      assertThatJson(response.body!!).isEqualTo(
        """
        [
          {
            "offenderNo": "A1179MT",
            "createDateTime": "2018-04-25T00:00:00",
            "fromAgency": "MDI",
            "fromAgencyDescription": "Moorland",
            "toAgency": "OUT",
            "toAgencyDescription": "Outside",
            "fromCity": "",
            "toCity": "",
            "movementType": "REL",
            "movementTypeDescription": "Release",
            "directionCode": "OUT",
            "movementDate": "2018-05-01",
            "movementTime": "13:00:00",
            "movementReason": "Abscond",
            "movementReasonCode": "UAL"
          },
          {
            "offenderNo": "A1179MT",
            "createDateTime": "2019-04-25T00:00:00",
            "fromAgency": "MDI",
            "fromAgencyDescription": "Moorland",
            "toAgency": "LEI",
            "toAgencyDescription": "Leeds",
            "fromCity": "Birmingham",
            "toCity": "Leicester",
            "movementType": "ADM",
            "movementTypeDescription": "Admission",
            "directionCode": "IN",
            "movementDate": "2019-05-01",
            "movementTime": "13:00:00",
            "movementReason": "Recapture (Escapee)",
            "movementReasonCode": "RECA"
          }
        ]
        """.trimIndent(),
      )
    }

    @Test
    fun `Retrieve a list of recent movements for offenders`() {
      val response = webTestClient.post()
        .uri("/api/movements/offenders?movementTypes=TRN&movementTypes=REL")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(listOf("A6676RS", "Z0021ZZ"))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(Movement::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "movementType",
        "fromAgencyDescription",
        "toAgencyDescription",
        "movementReason",
        "movementTime",
      )
        .containsExactlyInAnyOrder(
          Tuple("TRN", "Birmingham", "Moorland", "Normal Transfer", LocalTime.parse("12:00")),
          Tuple("REL", "Leeds", "Outside", "Abscond End of Custody Licence", LocalTime.parse("00:00")),
        )
    }

    @Test
    fun `Get brief information about most recent movements, specifically dealing with temporary absences`() {
      val response = webTestClient.post()
        .uri("/api/movements/offenders")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(listOf("A1181FF", "A6676RS"))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(Movement::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "offenderNo",
        "movementType",
        "fromAgencyDescription",
        "toAgencyDescription",
        "movementReason",
        "movementTime",
        "fromCity",
        "toCity",
      )
        .containsExactly(
          Tuple("A1181FF", "TAP", "", "Leeds", "Funerals And Deaths", LocalTime.parse("00:00"), "Wadhurst", ""),
          Tuple("A6676RS", "TAP", "Leeds", "", "Funerals And Deaths", LocalTime.parse("00:00"), "", "Wadhurst"),
        )
    }
  }

  @Nested
  @DisplayName("GET /api/movements/offender/{offenderNo}")
  inner class GetMovementsForOffender {
    @Test
    fun `Get movements across all bookings for an offender`() {
      webTestClient.get()
        .uri("/api/movements/offender/{offenderNo}?allBookings=true", "A1179MT")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "offenderNo": "A1179MT",
              "createDateTime": "2018-04-25T00:00:00",
              "fromAgency": "MDI",
              "fromAgencyDescription": "Moorland",
              "toAgency": "OUT",
              "toAgencyDescription": "Outside",
              "fromCity": "",
              "toCity": "",
              "movementType": "REL",
              "movementTypeDescription": "Release",
              "directionCode": "OUT",
              "movementDate": "2018-05-01",
              "movementTime": "13:00:00",
              "movementReason": "Abscond",
              "movementReasonCode": "UAL"
            },
            {
              "offenderNo": "A1179MT",
              "createDateTime": "2019-04-25T00:00:00",
              "fromAgency": "MDI",
              "fromAgencyDescription": "Moorland",
              "toAgency": "LEI",
              "toAgencyDescription": "Leeds",
              "fromCity": "Birmingham",
              "toCity": "Leicester",
              "movementType": "ADM",
              "movementTypeDescription": "Admission",
              "directionCode": "IN",
              "movementDate": "2019-05-01",
              "movementTime": "13:00:00",
              "movementReason": "Recapture (Escapee)",
              "movementReasonCode": "RECA"
            }
          ]
          """.trimIndent(),
        )
    }

    @Test
    fun `Get movements filtered by movement type for offender`() {
      webTestClient.get()
        .uri("/api/movements/offender/{offenderNo}?movementTypes=ADM", "A1179MT")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "offenderNo": "A1179MT",
              "createDateTime": "2019-04-25T00:00:00",
              "fromAgency": "MDI",
              "fromAgencyDescription": "Moorland",
              "toAgency": "LEI",
              "toAgencyDescription": "Leeds",
              "fromCity": "Birmingham",
              "toCity": "Leicester", 
              "movementType": "ADM",
              "movementTypeDescription": "Admission",
              "directionCode": "IN",
              "movementDate": "2019-05-01",
              "movementTime": "13:00:00",
              "movementReason": "Recapture (Escapee)",
              "movementReasonCode": "RECA"
            }
          ]
          """.trimIndent(),
        )
    }

    @Test
    fun `Get movements across filtered by date`() {
      webTestClient.get()
        .uri("/api/movements/offender/{offenderNo}?allBookings=true&movementsAfter=2018-06-01", "A1179MT")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "offenderNo": "A1179MT",
              "createDateTime": "2019-04-25T00:00:00",
              "fromAgency": "MDI",
              "fromAgencyDescription": "Moorland",
              "toAgency": "LEI",
              "toAgencyDescription": "Leeds",
              "fromCity": "Birmingham",
              "toCity": "Leicester",
              "movementType": "ADM",
              "movementTypeDescription": "Admission",
              "directionCode": "IN",
              "movementDate": "2019-05-01",
              "movementTime": "13:00:00",
              "movementReason": "Recapture (Escapee)",
              "movementReasonCode": "RECA"
            }
          ]
          """.trimIndent(),
        )
    }

    @Test
    fun `Get movements by date and movement type`() {
      webTestClient.get()
        .uri("/api/movements/offender/{offenderNo}?allBookings=true&movementsAfter=2018-06-01&movementTypes=ADM", "A1179MT")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "offenderNo": "A1179MT",
              "createDateTime": "2019-04-25T00:00:00",
              "fromAgency": "MDI",
              "fromAgencyDescription": "Moorland",
              "toAgency": "LEI",
              "toAgencyDescription": "Leeds",
              "fromCity": "Birmingham",
              "toCity": "Leicester",
              "movementType": "ADM",
              "movementTypeDescription": "Admission",
              "directionCode": "IN",
              "movementDate": "2019-05-01",
              "movementTime": "13:00:00",
              "movementReason": "Recapture (Escapee)",
              "movementReasonCode": "RECA"
            }
          ]
          """.trimIndent(),
        )
    }
  }

  @Nested
  @DisplayName("GET /api/movements/booking/{bookingId}")
  inner class GetMovementsForBooking {
    @Test
    fun `Get movements for booking`() {
      lateinit var booking: OffenderBookingId

      builder.build {
        offender {
          booking = booking(bookingInTime = LocalDateTime.parse("2025-05-18T08:00:00")) {
            release(releaseTime = LocalDateTime.parse("2025-05-19T10:00:00"))
            recall(recallTime = LocalDateTime.parse("2025-05-20T22:00:00"))
          }
        }
      }

      webTestClient.get()
        .uri("/api/movements/booking/{bookingId}", booking.bookingId)
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo("3")
        .jsonPath("[0].sequence").isEqualTo(1)
        .jsonPath("[0].fromAgency").isEqualTo("OUT")
        .jsonPath("[0].toAgency").isEqualTo("MDI")
        .jsonPath("[0].movementType").isEqualTo("ADM")
        .jsonPath("[0].directionCode").isEqualTo("IN")
        .jsonPath("[0].movementDateTime").isEqualTo("2025-05-18T08:00:00")
        .jsonPath("[0].movementReasonCode").isEqualTo("N")
        .jsonPath("[0].createdDateTime").isAboutNow()
        .jsonPath("[1].sequence").isEqualTo(2)
        .jsonPath("[1].fromAgency").isEqualTo("MDI")
        .jsonPath("[1].toAgency").isEqualTo("OUT")
        .jsonPath("[1].movementType").isEqualTo("REL")
        .jsonPath("[1].directionCode").isEqualTo("OUT")
        .jsonPath("[1].movementDateTime").isEqualTo("2025-05-19T10:00:00")
        .jsonPath("[1].movementReasonCode").isEqualTo("CR")
        .jsonPath("[1].createdDateTime").isAboutNow()
        .jsonPath("[2].sequence").isEqualTo(3)
        .jsonPath("[2].fromAgency").isEqualTo("OUT")
        .jsonPath("[2].toAgency").isEqualTo("MDI")
        .jsonPath("[2].movementType").isEqualTo("ADM")
        .jsonPath("[2].directionCode").isEqualTo("IN")
        .jsonPath("[2].movementDateTime").isEqualTo("2025-05-20T22:00:00")
        .jsonPath("[2].movementReasonCode").isEqualTo("24")
        .jsonPath("[2].createdDateTime").isAboutNow()
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
        .headers(setClientAuthorisation(listOf()))
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
      assertThatJson(response.body!!).isEqualTo("{\"in\":0,\"out\":2}")
    }

    @Test
    fun `Get a days movement count for a prison`() {
      val response = webTestClient.get()
        .uri("/api/movements/rollcount/MDI/movements?movementDate=2000-08-16")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk
        .expectBodyList(MovementCount::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting("in", "out").containsExactly(Tuple(2, 2))
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
        .headers(setClientAuthorisation(listOf()))
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

    @Test
    fun `Retrieve a list of en-route offenders`() {
      val response = webTestClient.get()
        .uri("/api/movements/LEI/enroute?movementDate=2017-10-12")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderMovement::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "offenderNo",
        "fromAgencyDescription",
        "toAgencyDescription",
        "movementTime",
        "movementReasonDescription",
        "lastName",
      )
        .containsExactlyInAnyOrder(
          Tuple("A1183AD", "Birmingham", "Leeds", LocalTime.parse("15:00"), "Normal Transfer", "DENTON"),
          Tuple("A1183SH", "Birmingham", "Leeds", LocalTime.parse("13:00"), "Normal Transfer", "HEMP"),
        )
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
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when user has no caseloads`() {
      webTestClient.get().uri("/api/movements/BMI/in/2019-01-10")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 404 when agency does not exist`() {
      webTestClient.get().uri("/api/movements/doesnotExist/in/2019-01-10")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/LEI/in/2019-01-10")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Get brief information for offenders 'in today'`() {
      val response = webTestClient.get()
        .uri("/api/movements/LEI/in/2017-10-12")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderIn::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "offenderNo",
        "bookingId",
        "dateOfBirth",
        "firstName",
        "middleName",
        "lastName",
        "fromAgencyDescription",
        "toAgencyDescription",
        "fromAgencyId",
        "toAgencyId",
        "movementTime",
        "movementDateTime",
        "location",
        "fromAddress",
      )
        .containsExactly(
          Tuple(
            "A6676RS",
            -29L,
            LocalDate.parse("1945-01-10"),
            "Neil",
            null,
            "Bradley",
            "Birmingham",
            "Leeds",
            "BMI",
            "LEI",
            LocalTime.parse("10:45"),
            LocalDateTime.parse("2017-10-12T10:45"),
            "LANDING H/1",
            "Birmingham Youth Court, Justice Avenue",
          ),
        )
    }

    @Test
    fun `Get brief information about offenders 'in today' specifically dealing with temporary absences`() {
      val response = webTestClient.get()
        .uri("/api/movements/LEI/in/2018-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderIn::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "offenderNo",
        "bookingId",
        "dateOfBirth",
        "firstName",
        "middleName",
        "lastName",
        "toAgencyDescription",
        "toAgencyId",
        "movementTime",
        "movementDateTime",
        "location",
        "fromCity",
      )
        .containsExactly(
          Tuple(
            "A1181FF",
            -47L,
            LocalDate.parse("1980-01-02"),
            "Janis",
            null,
            "Drp",
            "Leeds",
            "LEI",
            LocalTime.parse("00:00"),
            LocalDateTime.parse("2018-01-01T00:00"),
            null,
            "Wadhurst",
          ),
        )
    }

    @Test
    fun `Get information around an offender arriving and leaving multiple times on the same day 1`() {
      val response = webTestClient.get()
        .uri("/api/movements/MDI/in/2000-08-16")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderIn::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "offenderNo",
        "bookingId",
        "dateOfBirth",
        "firstName",
        "middleName",
        "lastName",
        "fromAgencyDescription",
        "toAgencyDescription",
        "fromAgencyId",
        "toAgencyId",
        "movementTime",
        "movementDateTime",
        "location",
      )
        .containsExactly(
          Tuple(
            "A1181FF",
            -47L,
            LocalDate.parse("1980-01-02"),
            "Janis",
            null,
            "Drp",
            "Outside",
            "Moorland",
            "OUT",
            "MDI",
            LocalTime.parse("00:00"),
            LocalDateTime.parse("2000-08-16T00:00"),
            null,
          ),
          Tuple(
            "A1181FF",
            -47L,
            LocalDate.parse("1980-01-02"),
            "Janis",
            null,
            "Drp",
            "Court 1",
            "Moorland",
            "COURT1",
            "MDI",
            LocalTime.parse("00:00"),
            LocalDateTime.parse("2000-08-16T00:00"),
            null,
          ),
        )
    }

    @Test
    fun `Get information around an offender arriving and leaving multiple times on the same day 3`() {
      val response = webTestClient.get()
        .uri("/api/movements/LEI/in/2000-08-16")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderIn::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "offenderNo",
        "bookingId",
        "dateOfBirth",
        "firstName",
        "middleName",
        "lastName",
        "fromAgencyDescription",
        "toAgencyDescription",
        "fromAgencyId",
        "toAgencyId",
        "movementTime",
        "movementDateTime",
        "location",
      )
        .containsExactly(
          Tuple(
            "A1181FF",
            -47L,
            LocalDate.parse("1980-01-02"),
            "Janis",
            null,
            "Drp",
            "Moorland",
            "Leeds",
            "MDI",
            "LEI",
            LocalTime.parse("00:00"),
            LocalDateTime.parse("2000-08-16T00:00"),
            null,
          ),
        )
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
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success when user has agency in caseload`() {
      webTestClient.get().uri("/api/movements/rollcount/LEI/in-reception")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Get offender in reception`() {
      val response = webTestClient.get()
        .uri("/api/movements/rollcount/MDI/in-reception")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderInReception::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting("bookingId", "offenderNo", "dateOfBirth", "firstName", "lastName")
        .containsExactly(Tuple(-46L, "A1181DD", LocalDate.parse("1980-01-02"), "Amy", "Dude"))
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
    fun `should return 403 when does not have agency in caseload`() {
      webTestClient.get().uri("/api/movements/BMI/in?fromDateTime=2019-01-10T10:35:17")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("/api/movements/BMI/in?fromDateTime=2019-01-10T10:35:17")
        .headers(setClientAuthorisation(listOf()))
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
        .headers(setAuthorisation(listOf()))
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
        .headers(setClientAuthorisation(listOf()))
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
        .headers(setClientAuthorisation(listOf()))
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
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when user has no caseloads`() {
      webTestClient.get().uri("/api/movements/LEI/out/2012-07-16")
        .headers(setAuthorisation("RO_USER", listOf()))
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

    @Test
    fun `Get brief information for offenders 'out today'`() {
      val response = webTestClient.get()
        .uri("/api/movements/LEI/out/2000-02-12")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderOutTodayDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "firstName",
        "lastName",
        "offenderNo",
        "dateOfBirth",
        "timeOut",
        "reasonDescription",
      )
        .containsExactly(
          Tuple(
            "Nick",
            "Talbot",
            "Z0018ZZ",
            LocalDate.parse("1970-01-01"),
            LocalTime.parse("12:00"),
            "Normal Transfer",
          ),
        )
    }

    @Test
    fun `Get information around an offender arriving and leaving multiple times on the same day 2`() {
      val response = webTestClient.get()
        .uri("/api/movements/MDI/out/2000-08-16")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBodyList(OffenderOutTodayDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response).extracting(
        "firstName",
        "lastName",
        "offenderNo",
        "dateOfBirth",
        "timeOut",
        "reasonDescription",
      )
        .containsExactlyInAnyOrder(
          Tuple("Janis", "Drp", "A1181FF", LocalDate.parse("1980-01-02"), LocalTime.parse("00:00"), "Normal Transfer"),
          Tuple("Janis", "Drp", "A1181FF", LocalDate.parse("1980-01-02"), LocalTime.parse("00:00"), "Normal Transfer"),
        )
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
  @TestInstance(PER_CLASS) // allows for a simple private MethodSource function
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

    @ParameterizedTest
    @MethodSource("getAgenciesAndTimes")
    fun `Get the details of the external movements between two times for a list of agencies`(row: MovementParameters) {
      val a1Param = if (row.agency1.isBlank()) "" else "&agencyId=${row.agency1}"
      val a2Param = if (row.agency2.isBlank()) "" else "&agencyId=${row.agency2}"
      val uri =
        "/api/movements/transfers?fromDateTime={fromTime}&toDateTime={toTime}$a1Param$a2Param&courtEvents=true&releaseEvents=true&transferEvents=true&movements=true"
      val response = webTestClient.get()
        .uri(uri, row.fromTime, row.toTime)
        .headers(setClientAuthorisation(listOf("GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        //  .expectBody().json("""{ "stuff": 0}""")
        .expectBody(TransferSummary::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response.courtEvents).hasSize(row.courtCount)
      assertThat(response.releaseEvents).hasSize(row.releaseCount)
      assertThat(response.transferEvents).hasSize(row.transferCount)
      assertThat(response.movements).hasSize(row.movementCount)
    }

    @ParameterizedTest
    @MethodSource("getAgenciesAndTimesValidation")
    fun `Get the details of the external movements between two times for a list of agencies - validation`(row: MovementParameters) {
      val a1Param = if (row.agency1.isBlank()) "" else "&agencyId=${row.agency1}"
      val a2Param = if (row.agency2.isBlank()) "" else "&agencyId=${row.agency2}"
      val uri =
        "/api/movements/transfers?fromDateTime={fromTime}&toDateTime={toTime}$a1Param$a2Param&courtEvents=true&releaseEvents=true&transferEvents=true&movements=true"
      val response = webTestClient.get()
        .uri(uri, row.fromTime, row.toTime)
        .headers(setClientAuthorisation(listOf("GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody(ErrorResponse::class.java)
        .returnResult()
        .responseBody!!

      assertThat(response.userMessage).isNotBlank()
    }

    @SuppressWarnings("unused")
    private fun getAgenciesAndTimes() = listOf(
      MovementParameters("LEI", "", "2019-05-01T11:00:00", "2019-05-01T18:00:00", 2, 1, 1, 0),
      MovementParameters("MDI", "LEI", "2019-05-01T00:00:00", "2019-05-01T00:00:00", 0, 0, 0, 1),
      MovementParameters("LEI", "MDI", "2019-05-01T11:00:00", "2019-05-01T18:00:00", 3, 1, 1, 1),
      MovementParameters("INVAL", "INVAL", "2019-05-01T11:00:00", "2019-05-01T18:00:00", 0, 0, 0, 0),
    )

    @SuppressWarnings("unused")
    private fun getAgenciesAndTimesValidation() = listOf(
      MovementParameters("LEI", "MDI", "2019-05-01T17:00:00", "2019-05-01T11:00:00", 0, 0, 0, 0),
      MovementParameters("LEI", "LEI", "2019-05-01TXX:XX:XX", "2019-05-01TXX:XX:XX", 0, 0, 0, 0),
      MovementParameters("", "", "2019-05-01T11:00:00", "2019-05-01T17:00:00", 0, 0, 0, 0),
    )

    private fun getScheduledMovements(
      courtEvents: Boolean,
      releaseEvents: Boolean,
      transferEvents: Boolean,
    ): ResponseEntity<String?> {
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
    ): ResponseEntity<String?> {
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
      [{"offenderNo":"Z0024ZZ","bookingId":-24,"dateOfBirth":"1958-01-01","firstName":"Lucius","lastName":"Fox","location":"LANDING H/1"},
       {"offenderNo":"Z0025ZZ","bookingId":-25,"dateOfBirth":"1974-01-01","firstName":"Matthew","lastName":"Smith","location":"LANDING H/1"}]
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
      [{"offenderNo":"Z0024ZZ","bookingId":-24,"dateOfBirth":"1958-01-01","firstName":"Lucius","lastName":"Fox","location":"LANDING H/1"},
       {"offenderNo":"Z0025ZZ","bookingId":-25,"dateOfBirth":"1974-01-01","firstName":"Matthew","lastName":"Smith","location":"LANDING H/1"}]
         """,
        )
    }
  }

  @Nested
  @DisplayName("GET /api/movements/offenders/{offenderNumber}/latest-arrival-date")
  inner class GetLatestArrivalDate {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/movements/offenders/Z0024ZZ/latest-arrival-date")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return latest arrival date`() {
      webTestClient.get().uri("/api/movements/offenders/Z0024ZZ/latest-arrival-date")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
            "2017-07-16"
          """.trimIndent(),
        )
    }

    @Test
    fun `should return when no movements`() {
      webTestClient.get().uri("/api/movements/offenders/Z0020XY/latest-arrival-date")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody().isEmpty
    }

    @Test
    fun `should return when offender not found`() {
      webTestClient.get().uri("/api/movements/offenders/Z0099ZZ/latest-arrival-date")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody().isEmpty
    }
  }

  @Nested
  @DisplayName("POST /api/movements/offenders/latest-arrival-date")
  inner class GetLatestArrivalDates {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post()
        .uri("/api/movements/offenders/latest-arrival-date")
        .bodyValue(listOf("Z0018ZZ", "Z0019ZZ", "Z0024ZZ"))
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return latest arrival dates`() {
      webTestClient.post()
        .uri("/api/movements/offenders/latest-arrival-date")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .bodyValue(listOf("Z0018ZZ", "Z0019ZZ", "Z0024ZZ"))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
            [
              {
                "offenderNo": "Z0018ZZ",
                "latestArrivalDate": "2012-07-05"
              },
              {
                "offenderNo": "Z0019ZZ",
                "latestArrivalDate": "2011-11-07"
              },
              {
                "offenderNo": "Z0024ZZ",
                "latestArrivalDate": "2017-07-16"
              }
            ]     
          """.trimIndent(),
        )
    }

    @Test
    fun `should return when no movements`() {
      webTestClient.post()
        .uri("/api/movements/offenders/latest-arrival-date")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .bodyValue(listOf("Z0020XY"))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `should return when offender not found`() {
      webTestClient.post()
        .uri("/api/movements/offenders/latest-arrival-date")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .bodyValue(listOf("Z0099ZZ"))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }
  }

  internal fun String.readFile(): String = this@MovementResourceTest::class.java.getResource(this)!!.readText()

  data class MovementParameters(
    val agency1: String,
    val agency2: String,
    val fromTime: String,
    val toTime: String,
    val movementCount: Int,
    val courtCount: Int,
    val transferCount: Int,
    val releaseCount: Int,
  )
}
