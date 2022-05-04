package uk.gov.justice.hmpps.prison.service.transfer

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import java.time.LocalDateTime

@Service
class BedAssignmentTransferService(private val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository) {
  fun createBedHistory(
    booking: OffenderBooking,
    cellLocation: AgencyInternalLocation,
    receiveTime: LocalDateTime,
    reasonCode: String? = null
  ): BedAssignmentHistory =
    bedAssignmentHistoriesRepository.save(
      BedAssignmentHistory
      (
        /* bedAssignmentHistoryPK = */ BedAssignmentHistoryPK(
          /* offenderBookingId = */ booking.bookingId,
          /* sequence = */ getNextSequence(booking)
        ),
        /* offenderBooking = */ booking,
        /* livingUnitId = */ cellLocation.locationId,
        /* assignmentDate = */ receiveTime.toLocalDate(),
        /* assignmentDateTime = */ receiveTime,
        /* assignmentReason = */ reasonCode,
        /* assignmentEndDate = */ null,
        /* assignmentEndDateTime = */ null
      )
    )

  private fun getNextSequence(booking: OffenderBooking) = bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.bookingId) + 1
}
