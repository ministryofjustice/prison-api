package uk.gov.justice.hmpps.prison.service.transfer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment.AUTO_TRANSFER_FROM_COURT_OR_TAP
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTeamAssignmentRepository

@Service
class TeamWorkflowNotificationService(
  private val offenderTeamAssignmentRepository: OffenderTeamAssignmentRepository,
  private val workflowTaskService: WorkflowTaskService
) {
  fun sendTransferViaCourtNotification(booking: OffenderBooking, updateMovement: () -> ExternalMovement): ExternalMovement {
    val offenderTeamAssignment = offenderTeamAssignmentRepository.findByIdOrNull(
      OffenderTeamAssignment.PK(
        booking,
        AUTO_TRANSFER_FROM_COURT_OR_TAP
      )
    )

    return updateMovement().also { movement ->
      offenderTeamAssignment
        ?.run {
          workflowTaskService.createTaskAutomaticTransfer(booking, movement, this.team)
        }
    }
  }
}
