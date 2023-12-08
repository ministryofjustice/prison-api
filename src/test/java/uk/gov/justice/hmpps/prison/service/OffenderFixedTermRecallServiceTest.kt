package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.FixedTermRecallDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFixedTermRecall
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFixedTermRecallRepository
import java.time.LocalDate
import java.util.Optional

/**
 * Test cases for [OffenderFixedTermRecallService].
 */
@ExtendWith(MockitoExtension::class)
class OffenderFixedTermRecallServiceTest {
  private val repository: OffenderFixedTermRecallRepository = mock()

  private lateinit var offenderFixedTermRecallService: OffenderFixedTermRecallService

  @BeforeEach
  fun init() {
    offenderFixedTermRecallService = OffenderFixedTermRecallService(repository)
  }

  @Test
  fun testGetFixedTermRecallDetails() {
    val bookingId = 1L
    val returnToCustodyDate = LocalDate.now()
    val recallLength = 14

    whenever(repository.findById(bookingId)).thenReturn(
      Optional.of(
        OffenderFixedTermRecall.builder()
          .returnToCustodyDate(returnToCustodyDate)
          .recallLength(recallLength)
          .offenderBooking(OffenderBooking.builder().bookingId(bookingId).build())
          .build(),
      ),
    )

    val result = offenderFixedTermRecallService.getFixedTermRecallDetails(bookingId)

    assertThat(result).isEqualTo(
      FixedTermRecallDetails
        .builder()
        .bookingId(bookingId)
        .returnToCustodyDate(returnToCustodyDate)
        .recallLength(recallLength)
        .build(),
    )
  }

  @Test
  fun testFixedTermRecallDetails_notFound() {
    val bookingId = 1L

    whenever(repository.findById(bookingId)).thenReturn(Optional.empty())

    assertThatThrownBy { offenderFixedTermRecallService.getFixedTermRecallDetails(bookingId) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("No fixed term recall found for booking 1")
  }
}
