package uk.gov.justice.hmpps.prison.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.storedprocs.WorkflowNotificationProcs.CreateWorkflowTask;

import java.time.LocalDateTime;

@Repository
public class WorkflowNotificationRepository extends RepositoryBase {

    private final CreateWorkflowTask createWorkflowTask;

    public WorkflowNotificationRepository(CreateWorkflowTask createWorkflowTask) {
        this.createWorkflowTask = createWorkflowTask;
    }

    public void createWorkflowTask(final long offenderBookId,
                                   final String movementReasonCode,
                                   final LocalDateTime movementDateTime,
                                   final String oldCaseloadId,
                                   final String newCaseloadId,
                                   final long teamId) {
        final var params = new MapSqlParameterSource()
            .addValue("p_offender_book_id", offenderBookId)
            .addValue("p_movement_rsn_code", movementReasonCode)
            .addValue("p_movement_date", movementDateTime.toLocalDate())
            .addValue("p_movement_time", movementDateTime.toLocalTime())
            .addValue("p_old_caseload_id", oldCaseloadId)
            .addValue("p_new_caseload_id", newCaseloadId)
            .addValue("p_team_id", teamId);


        createWorkflowTask.execute(params);
    }

}
