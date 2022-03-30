package uk.gov.justice.hmpps.prison.repository

import uk.gov.justice.hmpps.prison.repository.storedprocs.WorkflowNotificationProcs.CreateWorkflowTask
import uk.gov.justice.hmpps.prison.repository.RepositoryBase
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class WorkflowNotificationRepository(private val createWorkflowTask: CreateWorkflowTask) : RepositoryBase() {
    fun createWorkflowTask(
        offenderBookId: Long,
        movementReasonCode: String?,
        movementDateTime: LocalDateTime,
        oldCaseloadId: String?,
        newCaseloadId: String?,
        teamId: Long
    ) {
        val params = MapSqlParameterSource()
            .addValue("p_offender_book_id", offenderBookId)
            .addValue("p_movement_rsn_code", movementReasonCode)
            .addValue("p_movement_date", movementDateTime.toLocalDate())
            .addValue("p_movement_time", movementDateTime.toLocalTime())
            .addValue("p_old_caseload_id", oldCaseloadId)
            .addValue("p_new_caseload_id", newCaseloadId)
            .addValue("p_team_id", teamId)
        createWorkflowTask.execute(params)
    }
}
