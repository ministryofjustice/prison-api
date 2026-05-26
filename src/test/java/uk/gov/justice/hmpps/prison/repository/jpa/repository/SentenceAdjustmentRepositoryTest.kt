package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SentenceAdjustmentRepositoryTest(
  @Autowired private val repository: OffenderSentenceAdjustmentRepository,
) {
  @Test
  fun findAllForBooking() {
    val expected = listOf(
      SentenceAdjustment.builder()
        .id(-8L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("RSR")
        .active(true)
        .adjustDays(4)
        .build(),
      SentenceAdjustment.builder()
        .id(-9L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("RST")
        .active(false)
        .adjustDays(4)
        .build(),
      SentenceAdjustment.builder()
        .id(-10L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("RX")
        .active(true)
        .adjustDays(4)
        .build(),
      SentenceAdjustment.builder()
        .id(-11L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("S240A")
        .active(false)
        .adjustDays(4)
        .build(),
      SentenceAdjustment.builder()
        .id(-12L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("UR")
        .active(true)
        .adjustDays(4)
        .build(),
      SentenceAdjustment.builder()
        .id(-13L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("RX")
        .active(true)
        .adjustDays(4)
        .build(),
      SentenceAdjustment.builder()
        .id(-14L)
        .build(),
      SentenceAdjustment.builder()
        .id(-15L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("TCA")
        .active(true)
        .adjustDays(8)
        .build(),
      SentenceAdjustment.builder()
        .id(-16L)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .sentenceAdjustCode("TSA")
        .active(true)
        .adjustDays(9)
        .build(),
    )

    val sentenceAdjustments = repository.findAllByOffenderBooking_BookingId(-6L)

    assertThat(sentenceAdjustments).isEqualTo(expected)
  }
}
