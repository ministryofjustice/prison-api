package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import uk.gov.justice.hmpps.prison.api.model.CourtEventDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CourtServiceTest {

  val courtEventRepository: CourtEventRepository = mock()
  val offenderCourtCaseRepository: OffenderCourtCaseRepository = mock()
  var service = CourtService(courtEventRepository, offenderCourtCaseRepository)

  @Nested
  @DisplayName("Next Court Event tests")
  inner class NextCourtEvent {
    @Test
    fun `should return null record if no matching record in DB`() {
      whenever(
        courtEventRepository.findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(
          eq(BOOKING_ID),
          any(LocalDateTime::class.java),
          eq(Sort.by(Sort.Direction.ASC, "startTime")),
        ),
      ).thenReturn(Optional.empty())

      val result = service.getNextCourtEvent(BOOKING_ID)

      assertThat(result).isNull()
    }

    @Test
    fun `should return populated object when there is a match from the DB query`() {
      whenever(
        courtEventRepository.findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(
          eq(BOOKING_ID),
          any(LocalDateTime::class.java),
          eq(Sort.by(Sort.Direction.ASC, "startTime")),
        ),
      ).thenReturn(
        Optional.of(
          CourtEvent.builder()
            .id(COURT_EVENT_ID)
            .startTime(START_TIME)
            .courtLocation(AgencyLocation.builder().longDescription("Court 1").build())
            .courtEventType(MovementReason("CR", "Court Appearance"))
            .commentText("New court event").build(),
        ),
      )

      val result = service.getNextCourtEvent(BOOKING_ID)

      assertThat(result).isEqualTo(
        CourtEventDetails(
          eventId = COURT_EVENT_ID,
          startTime = START_TIME,
          courtLocation = "Court 1",
          courtEventType = "Court Appearance",
          comments = "New court event",
        ),
      )
    }
  }

  @Nested
  @DisplayName("Count of active cases tests")
  inner class ActiveCases {
    @Test
    fun `should return one when there is one active court case`() {
      whenever(
        offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(
          eq(BOOKING_ID),
        ),
      ).thenReturn(
        listOf(
          OffenderCourtCase.builder()
            .id(1L)
            .caseSeq(1)
            .caseStatus(ACTIVE_CASE_STATUS)
            .build(),
        ),
      )

      val result = service.getCountOfActiveCases(BOOKING_ID)

      assertThat(result).isEqualTo(1)
    }

    @Test
    fun `should return zero when there are no active court cases`() {
      whenever(
        offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(
          eq(BOOKING_ID),
        ),
      ).thenReturn(
        listOf(
          OffenderCourtCase.builder()
            .id(1L)
            .caseSeq(1)
            .build(),
        ),
      )

      val result = service.getCountOfActiveCases(BOOKING_ID)

      assertThat(result).isEqualTo(0)
    }
  }

  companion object {
    const val BOOKING_ID = 90001L
    const val COURT_EVENT_ID = 90002L
    val START_TIME: LocalDateTime = LocalDateTime.now()
    private val ACTIVE_CASE_STATUS = CaseStatus("A", "Active")
  }
}
