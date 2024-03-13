package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KeyDateAdjustmentRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderKeyDateAdjustmentRepository

  @Test
  fun findAllForBooking() {
    val expected = listOf(
      KeyDateAdjustment
        .builder()
        .id(-8L)
        .sentenceAdjustCode("ADA")
        .active(true)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .adjustDays(4)
        .build(),
      KeyDateAdjustment
        .builder()
        .id(-9L)
        .sentenceAdjustCode("ADA")
        .active(false)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .adjustDays(9)
        .build(),
      KeyDateAdjustment
        .builder()
        .id(-10L)
        .sentenceAdjustCode("ADA")
        .active(true)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .adjustDays(13)
        .build(),
      KeyDateAdjustment
        .builder()
        .id(-11L)
        .sentenceAdjustCode("UAL")
        .active(false)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .adjustDays(1)
        .build(),
      KeyDateAdjustment
        .builder()
        .id(-12L)
        .sentenceAdjustCode("RADA")
        .active(true)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .adjustDays(2)
        .build(),
      KeyDateAdjustment
        .builder()
        .id(-13L)
        .sentenceAdjustCode("UAL")
        .active(true)
        .offenderBooking(OffenderBooking.builder().bookingId(-6L).build())
        .adjustDays(7)
        .build(),
    )
    val keyDateAdjustments = repository.findAllByOffenderBooking_BookingId(-6L)
    assertThat(keyDateAdjustments).isEqualTo(expected)
  }
}
