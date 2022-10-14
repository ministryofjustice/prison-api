package uk.gov.justice.hmpps.prison.service.receiveandtransfer

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.repository.storedprocs.OffenderAdminProcs
import java.sql.Date
import java.time.format.DateTimeFormatter

interface WorkflowTaskService {
  fun createTaskAutomaticTransfer(booking: OffenderBooking, movement: ExternalMovement, team: Team)
}

@Service
@Profile("nomis")
class WorkflowTaskSPService(val createWorkflowTask: OffenderAdminProcs.CreateWorkflowTask) : WorkflowTaskService {
  override fun createTaskAutomaticTransfer(booking: OffenderBooking, movement: ExternalMovement, team: Team) {
    val offenderBookingId = booking.bookingId
    val movementReasonCode = movement.movementReason.code
    val movementDate = movement.movementDate
    val movementTime = movement.movementTime
    val oldCaseloadId = movement.fromAgency.id
    val newCaseloadId = movement.toAgency.id
    val teamId = team.id

    val params = MapSqlParameterSource()
      .addValue("p_offender_book_id", offenderBookingId)
      .addValue("p_movement_rsn_code", movementReasonCode)
      .addValue("p_movement_date", Date.valueOf(movementDate))
      .addValue("p_movement_time", movementTime.format(DateTimeFormatter.ofPattern("HH:mm")))
      .addValue("p_old_caseload_id", oldCaseloadId)
      .addValue("p_new_caseload_id", newCaseloadId)
      .addValue("p_team_id", teamId)

    createWorkflowTask.execute(params)
  }
}

@Service
@Profile("!nomis")
class WorkflowTaskNoopService : WorkflowTaskService {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  override fun createTaskAutomaticTransfer(booking: OffenderBooking, movement: ExternalMovement, team: Team) {
    log.warn("Not running against NOMIS database so will not create workflow task for court movement")
  }
}
