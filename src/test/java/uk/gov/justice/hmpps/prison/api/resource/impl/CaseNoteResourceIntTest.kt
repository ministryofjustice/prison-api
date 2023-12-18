package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerCaseNoteTypeAndSubType
import java.time.LocalDate
import java.time.LocalDateTime

class CaseNoteResourceIntTest : ResourceTest() {
  @MockBean
  private val caseNoteRepository: CaseNoteRepository? = null

  @MockBean
  private val offenderCaseNoteRepository: OffenderCaseNoteRepository? = null

  @Test
  fun `caseNoteEvents no limit`() {
    val fromDate = LocalDateTime.now()
    val fredEvent = createEvent("FRED", "JOE")
    val bobJoeEvent = createEvent("BOB", "JOE")
    whenever(
      caseNoteRepository!!.getCaseNoteEvents(
        ArgumentMatchers.any(),
        ArgumentMatchers.anySet(),
        ArgumentMatchers.anyLong(),
      ),
    ).thenReturn(
      listOf(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")),
    )

    val requestEntity =
      createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_CASE_NOTE_EVENTS"), mapOf())

    val responseEntity = testRestTemplate.exchange(
      "/api/case-notes/events_no_limit?type=BOB+JOE&type=FRED&createdDate=$fromDate",
      HttpMethod.GET,
      requestEntity,
      String::class.java,
    )

    assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json")

    Mockito.verify(caseNoteRepository).getCaseNoteEvents(fromDate, setOf("BOB", "FRED"), Long.MAX_VALUE)
  }

  @Test
  fun caseNoteEvents() {
    val fromDate = LocalDateTime.now()
    val fredEvent = createEvent("FRED", "JOE")
    val bobJoeEvent = createEvent("BOB", "JOE")
    whenever(
      caseNoteRepository!!.getCaseNoteEvents(
        ArgumentMatchers.any(),
        ArgumentMatchers.anySet(),
        ArgumentMatchers.anyLong(),
      ),
    ).thenReturn(
      listOf(bobJoeEvent, fredEvent, createEvent("BOB", "OTHER"), createEvent("WRONG", "TYPE")),
    )

    val requestEntity =
      createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_CASE_NOTE_EVENTS"), mapOf())

    val responseEntity = testRestTemplate.exchange(
      "/api/case-notes/events?limit=10&type=BOB+JOE&type=FRED&createdDate=$fromDate",
      HttpMethod.GET,
      requestEntity,
      String::class.java,
    )

    assertThatJsonFileAndStatus(responseEntity, 200, "casenoteevents.json")

    Mockito.verify(caseNoteRepository).getCaseNoteEvents(fromDate, setOf("BOB", "FRED"), 10)
  }

  @Test
  fun `caseNoteEvents missing limit`() {
    val requestEntity =
      createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_CASE_NOTE_EVENTS"), mapOf())
    val responseEntity = testRestTemplate.exchange(
      "/api/case-notes/events?&type=BOB+JOE&type=FRED&createdDate=" + LocalDateTime.now(),
      HttpMethod.GET,
      requestEntity,
      String::class.java,
    )
    assertThatJsonFileAndStatus(responseEntity, 400, "casenoteevents_validation.json")
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
      offenderCaseNoteRepository!!.findCaseNoteTypesByBookingAndDate(
        ArgumentMatchers.anyList(), ArgumentMatchers.anyList(),
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

    val types = listOf("POS", "NEG")

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
      "ITAG_USER", listOf("ROLE_VIEW_CASE_NOTES"),
      CaseNoteTypeSummaryRequest.builder()
        .types(types)
        .bookingFromDateSelection(bookingDatePairs)
        .build(),
    )

    val responseEntity = testRestTemplate.exchange(
      "/api/case-notes/usage-by-types", HttpMethod.POST,
      requestEntity,
      object : ParameterizedTypeReference<String?>() {
      },
    )

    assertThatJsonFileAndStatus(responseEntity, 200, "case_note_usage_by_type.json")
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
