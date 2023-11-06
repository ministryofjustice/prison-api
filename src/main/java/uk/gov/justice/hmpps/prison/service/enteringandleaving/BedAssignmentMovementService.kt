package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import java.time.LocalDateTime

@Service
class BedAssignmentMovementService(private val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository) {
  fun createBedHistory(
    booking: OffenderBooking,
    cellLocation: AgencyInternalLocation,
    receiveTime: LocalDateTime,
    reasonCode: String? = null,
  ): BedAssignmentHistory =
    bedAssignmentHistoriesRepository.save(
      BedAssignmentHistory.builder()
        .bedAssignmentHistoryPK(
          BedAssignmentHistoryPK(
            booking.bookingId,
            getNextSequence(booking),
          ),
        )
        .offenderBooking(booking)
        .livingUnitId(cellLocation.locationId)
        .location(cellLocation)
        .assignmentDate(receiveTime.toLocalDate())
        .assignmentDateTime(receiveTime)
        .assignmentReason(reasonCode)
        .build(),
    )

  private fun getNextSequence(booking: OffenderBooking) = bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.bookingId) + 1
}
