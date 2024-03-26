package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTeamAssignmentRepository
import java.time.LocalDate

@Component
class TeamAssignmentBuilderRepository(
  private val offenderBookingRepository: OffenderBookingRepository,
  private val offenderTeamAssignmentRepository: OffenderTeamAssignmentRepository,
) {
  fun save(
    teamToAssign: Team,
    bookingId: Long,
    functionTypeCode: String,
  ): OffenderTeamAssignment {
    val offenderBooking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow()
    return offenderTeamAssignmentRepository.save(
      OffenderTeamAssignment().apply {
        team = teamToAssign
        assignmentDate = LocalDate.now()
        id = OffenderTeamAssignment.PK(offenderBooking, functionTypeCode)
      },
    )
  }
}

@Component
class TeamAssignmentBuilderFactory(
  private val repository: TeamAssignmentBuilderRepository,
) {

  fun builder(): TeamAssignmentBuilder {
    return TeamAssignmentBuilder(repository)
  }
}

@NomisDataDslMarker
class TeamAssignmentBuilder(
  private val repository: TeamAssignmentBuilderRepository,
) {
  fun build(
    offenderBookingId: OffenderBookingId,
    teamToAssign: Team,
    functionTypeCode: String,
  ) = repository.save(
    bookingId = offenderBookingId.bookingId,
    teamToAssign = teamToAssign,
    functionTypeCode = functionTypeCode,
  )
}
