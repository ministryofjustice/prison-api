package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment.AUTO_TRANSFER_FROM_COURT_OR_TAP
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.time.LocalDate

class OffenderTeamAssignmentBuilder(
  private val team: Team,
  var functionTypeCode: String = AUTO_TRANSFER_FROM_COURT_OR_TAP,
) {

  fun save(
    offenderBookingId: Long,
    dataLoader: DataLoaderRepository,
  ): OffenderTeamAssignment {
    val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(offenderBookingId).orElseThrow()
    val teamToAssign = team

    return dataLoader.offenderTeamAssignmentRepository.save(
      OffenderTeamAssignment().apply {
        team = teamToAssign
        assignmentDate = LocalDate.now()
        id = OffenderTeamAssignment.PK(offenderBooking, functionTypeCode)
      },
    )
  }
}
