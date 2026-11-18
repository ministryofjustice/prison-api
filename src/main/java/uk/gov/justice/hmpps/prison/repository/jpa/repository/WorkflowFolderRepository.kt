package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.hmpps.prison.api.model.WorkflowFolderResponse
import uk.gov.justice.hmpps.prison.repository.jpa.model.WorkflowFolder

interface WorkflowFolderRepository : JpaRepository<WorkflowFolder, String> {
  @Query(
    value = """
      SELECT new uk.gov.justice.hmpps.prison.api.model.WorkflowFolderResponse(
        f.workflowCode, f.description, f.workflowSeq, f.caseloadType,
        f.createDatetime, f.createUserId, f.modifyDatetime, f.modifyUserId,
        f.auditTimestamp, f.auditUserId, f.auditModuleName, f.auditClientUserId,
        f.auditClientIpAddress, f.auditClientWorkstationName, f.auditAdditionalInfo
      )
      FROM WorkflowFolder f
      ORDER BY f.workflowCode
    """,
    countQuery = "SELECT COUNT(f) FROM WorkflowFolder f",
  )
  fun findFolders(pageable: Pageable): Page<WorkflowFolderResponse>
}
