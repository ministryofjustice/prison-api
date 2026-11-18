package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.hmpps.prison.api.model.WorkflowScreenResponse
import uk.gov.justice.hmpps.prison.repository.jpa.model.WorkflowScreen
import uk.gov.justice.hmpps.prison.repository.jpa.model.WorkflowScreen.PK

interface WorkflowScreenRepository : JpaRepository<WorkflowScreen, PK> {
  @Query(
    value = """
      SELECT new uk.gov.justice.hmpps.prison.api.model.WorkflowScreenResponse(
        s.id.workflowCode, s.id.moduleName, s.description, s.workflowSeq, s.caseloadType,
        s.createDatetime, s.createUserId, s.modifyDatetime, s.modifyUserId,
        s.auditTimestamp, s.auditUserId, s.auditModuleName, s.auditClientUserId,
        s.auditClientIpAddress, s.auditClientWorkstationName, s.auditAdditionalInfo
      )
      FROM WorkflowScreen s
      ORDER BY s.id.workflowCode, s.id.moduleName
    """,
    countQuery = "SELECT COUNT(s) FROM WorkflowScreen s",
  )
  fun findScreens(pageable: Pageable): Page<WorkflowScreenResponse>
}
