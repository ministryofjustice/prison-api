package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsageRequest
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerCaseNoteTypeAndSubType
import java.time.LocalDate
import java.time.LocalDateTime

class CaseNoteResourceIntTest : ResourceTest() {
  @MockBean
  private lateinit var caseNoteRepository: CaseNoteRepository

  @MockBean
  private lateinit var offenderCaseNoteRepository: OffenderCaseNoteRepository

  @Nested
  @DisplayName("GET /case-notes/staff-usage")
  inner class StaffUsageGet {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/case-notes/staff-usage")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/case-notes/staff-usage?staffId=123")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("POST /case-notes/staff-usage")
  inner class StaffUsagePost {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("api/case-notes/staff-usage")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.post().uri("api/case-notes/staff-usage")
        .headers(setClientAuthorisation(emptyList()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(CaseNoteStaffUsageRequest.builder().build()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("GET /case-notes/usage")
  inner class UsageGet {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/case-notes/usage")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/case-notes/usage")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("POST /case-notes/usage")
  inner class UsagePost {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("api/case-notes/usage")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.post().uri("api/case-notes/usage")
        .headers(setClientAuthorisation(emptyList()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(CaseNoteStaffUsageRequest.builder().build()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("GET /case-notes/events_no_limit")
  inner class EventsNoLimit {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/case-notes/events_no_limit")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/case-notes/events_no_limit?type=FRED&createdDate=2023-01-01T00:00")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `caseNoteEvents no limit`() {
      val fromDate = LocalDateTime.now()
      val fredEvent = createEvent("FRED", "JOE")
      val bobJoeEvent = createEvent("BOB", "JOE")
      whenever(
        caseNoteRepository.getCaseNoteEvents(
          ArgumentMatchers.any(),
          ArgumentMatchers.anySet(),
          ArgumentMatchers.anyLong(),
        ),
      ).thenReturn(
        listOf(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")),
      )

      webTestClient.get()
        .uri("/api/case-notes/events_no_limit?type=BOB+JOE&type=FRED&createdDate=$fromDate")
        .headers(setAuthorisation(listOf("ROLE_CASE_NOTE_EVENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "nomsId": "123JOE",
              "id": 1,
              "content": "Some content for JOE",
              "contactTimestamp": "2019-02-01T23:22:21",
              "notificationTimestamp": "2019-02-01T23:22:21",
              "establishmentCode": "LEI",
              "noteType": "BOB JOE",
              "staffName": "Last, First"
            },
            {
              "nomsId": "123JOE",
              "id": 1,
              "content": "Some content for JOE",
              "contactTimestamp": "2019-02-01T23:22:21",
              "notificationTimestamp": "2019-02-01T23:22:21",
              "establishmentCode": "LEI",
              "noteType": "FRED JOE",
              "staffName": "Last, First"
            }
          ]
          """.trimIndent(),
        )

      Mockito.verify(caseNoteRepository).getCaseNoteEvents(fromDate, setOf("BOB", "FRED"), Long.MAX_VALUE)
    }
  }

  @Nested
  @DisplayName("GET /case-notes/events")
  inner class Events {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/case-notes/events")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/case-notes/events?limit=20&type=FRED&createdDate=2023-01-01T00:00")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun caseNoteEvents() {
      val fromDate = LocalDateTime.now()
      val fredEvent = createEvent("FRED", "JOE")
      val bobJoeEvent = createEvent("BOB", "JOE")
      whenever(
        caseNoteRepository.getCaseNoteEvents(
          ArgumentMatchers.any(),
          ArgumentMatchers.anySet(),
          ArgumentMatchers.anyLong(),
        ),
      ).thenReturn(
        listOf(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")),
      )

      webTestClient.get()
        .uri("/api/case-notes/events?limit=10&type=BOB+JOE&type=FRED&createdDate=$fromDate")
        .headers(setAuthorisation(listOf("ROLE_CASE_NOTE_EVENTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "nomsId": "123JOE",
              "id": 1,
              "content": "Some content for JOE",
              "contactTimestamp": "2019-02-01T23:22:21",
              "notificationTimestamp": "2019-02-01T23:22:21",
              "establishmentCode": "LEI",
              "noteType": "BOB JOE",
              "staffName": "Last, First"
            },
            {
              "nomsId": "123JOE",
              "id": 1,
              "content": "Some content for JOE",
              "contactTimestamp": "2019-02-01T23:22:21",
              "notificationTimestamp": "2019-02-01T23:22:21",
              "establishmentCode": "LEI",
              "noteType": "FRED JOE",
              "staffName": "Last, First"
            }
          ]
          """.trimIndent(),
        )

      Mockito.verify(caseNoteRepository).getCaseNoteEvents(fromDate, setOf("BOB", "FRED"), 10)
    }

    @Test
    fun `caseNoteEvents missing limit`() {
      webTestClient.get()
        .uri("/api/case-notes/events?type=BOB+JOE&type=FRED&createdDate=${LocalDateTime.now()}")
        .headers(setAuthorisation(listOf("ROLE_CASE_NOTE_EVENTS")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Required request parameter 'limit' for method parameter type Long is not present")
        .json(
          """
          {
            "status": 400,
            "userMessage": "Required request parameter 'limit' for method parameter type Long is not present",
            "developerMessage": "Required request parameter 'limit' for method parameter type Long is not present"
          }
          """.trimIndent(),
        )
    }
  }

  @Nested
  @DisplayName("POST /case-notes/usage-by-types")
  inner class UsageByTypes {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("api/case-notes/usage-by-types")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.post().uri("api/case-notes/usage-by-types")
        .headers(setClientAuthorisation(emptyList()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(CaseNoteTypeSummaryRequest.builder().build()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("GET /case-notes/summary")
  inner class Summary {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/case-notes/summary")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/case-notes/summary?bookingId=123")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun caseNoteByTypesAndDates() {
      val fromDate1 = LocalDateTime.of(2018, 2, 3, 12, 0, 0)
      val fromDate2 = LocalDateTime.of(2019, 2, 3, 12, 0, 0)
      val fromDate3 = LocalDateTime.of(2020, 2, 3, 12, 0, 0)

      val bookingDatePairs = listOf(
        BookingFromDatePair.builder().bookingId(-16L).fromDate(fromDate1).build(),
        BookingFromDatePair.builder().bookingId(-17L).fromDate(fromDate2).build(),
        BookingFromDatePair.builder().bookingId(-18L).fromDate(fromDate3).build(),
      )

      whenever(
        offenderCaseNoteRepository.findCaseNoteTypesByBookingAndDate(
          ArgumentMatchers.anyList(),
          ArgumentMatchers.anyList(),
          ArgumentMatchers.any(
            LocalDate::class.java,
          ),
        ),
      ).thenReturn(
        listOf(
          PrisonerCaseNoteTypeAndSubType(-16L, "POS", "IEP_ENC", fromDate1.minusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-16L, "POS", "IEP_ENC", fromDate1.plusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-16L, "POS", "IEP_ENC", fromDate1.plusDays(2)),
          PrisonerCaseNoteTypeAndSubType(-16L, "NEG", "IEP_WARN", fromDate1),
          PrisonerCaseNoteTypeAndSubType(-16L, "NEG", "IEP_WARN", fromDate1.plusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-16L, "NEG", "IEP_WARN", fromDate1.plusDays(2)),

          PrisonerCaseNoteTypeAndSubType(-17L, "POS", "IEP_ENC", fromDate2.minusDays(5)),
          PrisonerCaseNoteTypeAndSubType(-17L, "POS", "IEP_ENC", fromDate2.minusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-17L, "POS", "IEP_ENC", fromDate2.plusDays(2)),
          PrisonerCaseNoteTypeAndSubType(-17L, "NEG", "IEP_WARN", fromDate2.minusDays(5)),
          PrisonerCaseNoteTypeAndSubType(-17L, "NEG", "IEP_WARN", fromDate2.plusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-17L, "NEG", "IEP_WARN", fromDate2.plusDays(2)),

          PrisonerCaseNoteTypeAndSubType(-18L, "POS", "IEP_ENC", fromDate3.minusDays(5)),
          PrisonerCaseNoteTypeAndSubType(-18L, "POS", "IEP_ENC", fromDate3.minusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-18L, "POS", "IEP_ENC", fromDate3.plusDays(2)),
          PrisonerCaseNoteTypeAndSubType(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(5)),
          PrisonerCaseNoteTypeAndSubType(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(1)),
          PrisonerCaseNoteTypeAndSubType(-18L, "NEG", "IEP_WARN", fromDate3.minusDays(2)),
        ),
      )

      webTestClient.post().uri("/api/case-notes/usage-by-types")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_VIEW_CASE_NOTES")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .body(
          BodyInserters.fromValue(
            CaseNoteTypeSummaryRequest.builder()
              .types(listOf("POS", "NEG"))
              .bookingFromDateSelection(bookingDatePairs)
              .build(),
          ),
        )
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {
              "bookingId": -16,
              "caseNoteType": "POS",
              "caseNoteSubType": "IEP_ENC",
              "numCaseNotes": 2
            },
            {
              "bookingId": -16,
              "caseNoteType": "NEG",
              "caseNoteSubType": "IEP_WARN",
              "numCaseNotes": 3
            },
            {
              "bookingId": -17,
              "caseNoteType": "POS",
              "caseNoteSubType": "IEP_ENC",
              "numCaseNotes": 1
            },
            {
              "bookingId": -17,
              "caseNoteType": "NEG",
              "caseNoteSubType": "IEP_WARN",
              "numCaseNotes": 2
            },
            {
              "bookingId": -18,
              "caseNoteType": "POS",
              "caseNoteSubType": "IEP_ENC",
              "numCaseNotes": 1
            }
          ]
          """.trimIndent(),
        )
    }
  }

  private fun createEvent(type: String, subType: String): CaseNoteEvent {
    return CaseNoteEvent.builder()
      .mainNoteType(type)
      .subNoteType(subType)
      .content("Some content for $subType")
      .contactTimestamp(LocalDateTime.parse("2019-02-01T23:22:21"))
      .notificationTimestamp(LocalDateTime.parse("2019-02-01T23:22:21"))
      .establishmentCode("LEI")
      .firstName("FIRST")
      .lastName("LAST")
      .id(1L)
      .nomsId("123$subType")
      .build()
  }
}
