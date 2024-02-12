package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CourtServiceTest {

  val repository: CourtEventRepository = mock()
  var service = CourtService(repository)

  @Test
  fun `should return null record if no matching record in DB`() {
    whenever(
      repository.findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(
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
      repository.findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(
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

  companion object {
    const val BOOKING_ID = 90001L
    const val COURT_EVENT_ID = 90002L
    val START_TIME: LocalDateTime = LocalDateTime.now()
  }
}
