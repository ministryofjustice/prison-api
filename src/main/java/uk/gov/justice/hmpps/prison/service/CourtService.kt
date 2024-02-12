package uk.gov.justice.hmpps.prison.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.CourtEventDetails
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class CourtService(
  private val courtEventRepository: CourtEventRepository,
) {
  fun getNextCourtEvent(bookingId: Long): CourtEventDetails? {
    val nextCourtEvent = courtEventRepository.findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(
      bookingId,
      LocalDateTime.now(),
      Sort.by(Sort.Direction.ASC, "startTime"),
    ).orElse(null) ?: return null

    return CourtEventDetails(
      eventId = nextCourtEvent.id,
      startTime = nextCourtEvent.startTime,
      comments = nextCourtEvent.commentText,
      caseReference = nextCourtEvent.offenderCourtCase.orElse(null)?.caseInfoNumber,
      courtLocation = nextCourtEvent.courtLocation.longDescription,
      courtEventType = nextCourtEvent.courtEventType.description,
    )
  }
}
