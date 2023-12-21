package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsageRequest
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerCaseNoteTypeAndSubType
import java.time.LocalDate
import java.time.LocalDateTime

class CaseNoteResourceIntTest : ResourceTest() {
  @MockBean
  private lateinit var offenderCaseNoteRepository: OffenderCaseNoteRepository

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
}
