package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeCount
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair
import uk.gov.justice.hmpps.prison.api.resource.CaseNoteResource
import uk.gov.justice.hmpps.prison.service.CaseNoteService
import java.time.LocalDateTime

class CaseNoteResourceTest {
  private val caseNoteService = mock<CaseNoteService>()
  private val caseNoteResource = CaseNoteResource(caseNoteService)

  @Test
  fun getCaseNoteUsageByBookingIdTypeAndDate() {
    val usage = listOf(
      CaseNoteTypeCount(-16L, "POS", "IEP_ENC", 2L),
      CaseNoteTypeCount(-16L, "NEG", "IEP_WARN", 3L),
      CaseNoteTypeCount(-17L, "POS", "IEP_ENC", 1L),
    )
    whenever(caseNoteService.getCaseNoteUsageByBookingIdTypeAndDate(anyList(), anyList())).thenReturn(usage)
    val bookingDatePairs = listOf(
      BookingFromDatePair.builder().bookingId(-16L).fromDate(LocalDateTime.parse("2017-05-13T12:00")).build(),
      BookingFromDatePair.builder().bookingId(-17L).fromDate(LocalDateTime.parse("2018-05-13T12:00")).build(),
    )
    val types = listOf("POS", "NEG")
    assertThat(
      caseNoteResource.getCaseNoteUsageSummaryByDates(
        CaseNoteTypeSummaryRequest.builder()
          .types(types)
          .bookingFromDateSelection(bookingDatePairs)
          .build(),
      ),
    ).isEqualTo(usage)
    verify(caseNoteService).getCaseNoteUsageByBookingIdTypeAndDate(types, bookingDatePairs)
  }
}
