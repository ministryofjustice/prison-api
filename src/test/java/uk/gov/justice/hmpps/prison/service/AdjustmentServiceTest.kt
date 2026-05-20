package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment
import uk.gov.justice.hmpps.prison.api.model.BookingAndSentenceAdjustments
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentValues
import uk.gov.justice.hmpps.prison.api.support.BookingAdjustmentType
import uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import java.time.LocalDate
import java.util.Optional

class AdjustmentServiceTest {
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val service = AdjustmentService(offenderBookingRepository)

  private companion object {
    private const val BOOKING_ID = 1L
  }

  @Test
  fun getBookingAndSentenceAdjustmentsReturnsCorrectData() {
    val sentenceAdjustment = SentenceAdjustment.builder()
      .sentenceSeq(911)
      .active(true)
      .sentenceAdjustCode("RSR")
      .adjustDays(4)
      .adjustFromDate(LocalDate.of(2022, 1, 1))
      .adjustToDate(LocalDate.of(2022, 1, 4))
      .build()

    val keyDateAdjustment = KeyDateAdjustment.builder()
      .active(true)
      .sentenceAdjustCode("ADA")
      .adjustDays(4)
      .adjustFromDate(LocalDate.of(2022, 1, 1))
      .adjustToDate(LocalDate.of(2022, 1, 4))
      .build()

    whenever(offenderBookingRepository.findById(BOOKING_ID)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .sentenceAdjustments(listOf(sentenceAdjustment))
          .keyDateAdjustments(listOf(keyDateAdjustment))
          .build(),
      ),
    )

    val bookingAndSentenceAdjustments = service.getBookingAndSentenceAdjustments(BOOKING_ID)

    val expected = BookingAndSentenceAdjustments.builder()
      .sentenceAdjustments(
        listOf(
          SentenceAdjustmentValues
            .builder()
            .sentenceSequence(911)
            .active(true)
            .type(SentenceAdjustmentType.RECALL_SENTENCE_REMAND)
            .numberOfDays(4)
            .fromDate(LocalDate.of(2022, 1, 1))
            .toDate(LocalDate.of(2022, 1, 4))
            .build(),
        ),
      )
      .bookingAdjustments(
        listOf(
          BookingAdjustment
            .builder()
            .active(true)
            .type(BookingAdjustmentType.ADDITIONAL_DAYS_AWARDED)
            .numberOfDays(4)
            .fromDate(LocalDate.of(2022, 1, 1))
            .toDate(LocalDate.of(2022, 1, 4))
            .build(),
        ),
      )
      .build()

    assertThat(bookingAndSentenceAdjustments).isEqualTo(expected)
  }

  @Test
  fun getBookingAndSentenceAdjustmentsReturnsNoData() {
    whenever(offenderBookingRepository.findById(BOOKING_ID)).thenReturn(
      Optional.of(OffenderBooking.builder().build()),
    )

    val bookingAndSentenceAdjustments = service.getBookingAndSentenceAdjustments(BOOKING_ID)

    val expected = BookingAndSentenceAdjustments.builder()
      .sentenceAdjustments(mutableListOf<SentenceAdjustmentValues?>())
      .bookingAdjustments(mutableListOf<BookingAdjustment?>())
      .build()

    assertThat(bookingAndSentenceAdjustments).isEqualTo(expected)
  }
}
