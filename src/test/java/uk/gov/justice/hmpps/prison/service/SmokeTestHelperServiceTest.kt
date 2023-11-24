@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.resource.UpdatePrisonerDetails
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class SmokeTestHelperServiceTest {
  private val bookingService: BookingService = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val prisonerReleaseAndTransferService: PrisonerReleaseAndTransferService = mock()
  private val bookingIntoPrisonService: BookingIntoPrisonService = mock()
  private val smokeTestHelperService = SmokeTestHelperService(
    bookingService,
    offenderBookingRepository,
    prisonerReleaseAndTransferService,
    bookingIntoPrisonService,
    offenderRepository,
  )

  @Nested
  internal inner class imprisonmentDataSetup {
    @Nested
    internal inner class NotFound {
      @Test
      fun noOffender() {
        whenever(bookingService.getOffenderIdentifiers(eq(SOME_OFFENDER_NO), anyString()))
          .thenThrow(EntityNotFoundException.withId(SOME_OFFENDER_NO))
        assertThatThrownBy { smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Resource with id [$SOME_OFFENDER_NO] not found.")
      }

      @Test
      fun noBooking() {
        whenever(bookingService.getOffenderIdentifiers(eq(SOME_OFFENDER_NO), anyString()))
          .thenReturn(OffenderBookingIdSeq(SOME_OFFENDER_NO, null, null))
        assertThatThrownBy { smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("No booking found for offender $SOME_OFFENDER_NO")
      }
    }

    @Nested
    internal inner class NoImprisonmentStatus {
      @Test
      fun notFoundRequest() {
        mockOffenderBooking()
        whenever(
          offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(SOME_OFFENDER_NO, SOME_BOOKING_SEQ),
        )
          .thenReturn(Optional.empty())
        assertThatThrownBy { smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("No booking found for offender $SOME_OFFENDER_NO and seq $SOME_BOOKING_SEQ")
      }

      @Test
      fun ignoresInactiveStatuses() {
        mockOffenderBooking()
        mockImprisonmentStatus("N")
        assertThatThrownBy { smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO) }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Unable to find active imprisonment status for offender number $SOME_OFFENDER_NO")
      }

      @Test
      fun oldStatusNotUpdated() {
        mockOffenderBooking()
        val offenderBooking = mockImprisonmentStatus("N")
        assertThatThrownBy { smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO) }
          .isInstanceOf(BadRequestException::class.java)
        assertThat(offenderBooking.imprisonmentStatuses[0].latestStatus).isEqualTo("N")
        assertThat(offenderBooking.imprisonmentStatuses[0].expiryDate.toLocalDate())
          .isEqualTo(LocalDate.now().minusDays(1L))
      }
    }

    @Nested
    internal inner class Ok {
      @Test
      fun savesNewStatus() {
        mockOffenderBooking()
        val offenderBooking = mockImprisonmentStatus("Y")
        smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO)
        val activeImprisonmentStatus = offenderBooking.activeImprisonmentStatus
        assertThat(activeImprisonmentStatus).isPresent()
        assertThat(activeImprisonmentStatus.get().imprisonStatusSeq).isEqualTo(2L)
        assertThat(activeImprisonmentStatus.get().effectiveDate).isEqualTo(LocalDate.now())
        assertThat(activeImprisonmentStatus.get().effectiveTime.toLocalDate()).isEqualTo(LocalDate.now())
      }

      @Test
      fun updatesOldStatus() {
        mockOffenderBooking()
        val oldImprisonmentStatus = mockImprisonmentStatus("Y").activeImprisonmentStatus.get()
        smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO)
        assertThat(oldImprisonmentStatus.latestStatus).isEqualTo("N")
        assertThat(oldImprisonmentStatus.expiryDate.toLocalDate()).isEqualTo(LocalDate.now())
      }
    }
  }

  @Nested
  internal inner class updatePrisonerDetails {
    @Test
    fun notFoundRequest() {
      assertThatThrownBy { smokeTestHelperService.updatePrisonerDetails(SOME_OFFENDER_NO, UpdatePrisonerDetails("joe", "bloggs")) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Offender $SOME_OFFENDER_NO not found")
    }

    @Test
    fun ok() {
      whenever(offenderRepository.findOffenderByNomsId(any()))
        .thenReturn(Optional.of(Offender().apply { firstName = "Joe"; lastName = "Bloggs" }))

      smokeTestHelperService.updatePrisonerDetails(SOME_OFFENDER_NO, UpdatePrisonerDetails("Fred", "Smith"))
      verify(offenderRepository).save(
        check {
          assertThat(it.firstName).isEqualTo("FRED")
          assertThat(it.lastName).isEqualTo("SMITH")
        },
      )
    }
  }

  private fun mockOffenderBooking() {
    whenever(bookingService.getOffenderIdentifiers(eq(SOME_OFFENDER_NO), anyString()))
      .thenReturn(SOME_BOOKING_ID_SEQ)
  }

  private fun mockImprisonmentStatus(latestStatus: String): OffenderBooking = OffenderBooking.builder()
    .imprisonmentStatuses(mutableListOf(anOffenderImprisonmentStatus(latestStatus)))
    .build().also {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(SOME_OFFENDER_NO, SOME_BOOKING_SEQ))
        .thenReturn(Optional.of(it))
    }

  private fun anOffenderImprisonmentStatus(latestStatus: String): OffenderImprisonmentStatus {
    val expiryDate = if (latestStatus == "Y") null else LocalDateTime.now().minusDays(1)
    return OffenderImprisonmentStatus(
      OffenderBooking.builder().bookingId(SOME_BOOKING_ID).build(),
      1L, ImprisonmentStatus.builder().status("status").build(),
      LocalDate.now().minusDays(1),
      LocalDateTime.now().minusDays(1),
      expiryDate,
      "LEI",
      "Comment",
      latestStatus,
      LocalDate.now().minusDays(1),
    )
  }

  companion object {
    private const val SOME_OFFENDER_NO = "A1060AA"
    private const val SOME_BOOKING_ID = 11L
    private const val SOME_BOOKING_SEQ = 1
    private val SOME_BOOKING_ID_SEQ = OffenderBookingIdSeq(SOME_OFFENDER_NO, SOME_BOOKING_ID, SOME_BOOKING_SEQ)
  }
}
