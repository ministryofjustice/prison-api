package uk.gov.justice.hmpps.prison.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.OffenderTeamAssignmentResponse
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTeamAssignmentRepository

@Service
@Transactional(readOnly = true)
class OffenderTeamAssignmentService(
  private val repository: OffenderTeamAssignmentRepository,
  @Value("\${spring.data.web.pageable.max-page-size:5000}")
  private val maxPageSize: Int,
) {
  fun getAssignments(page: Int, size: Int): Page<OffenderTeamAssignmentResponse> = repository.findAssignments(
    PageRequest.of(page, size.coerceAtMost(maxPageSize)),
  )
}
