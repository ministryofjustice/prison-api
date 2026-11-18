package uk.gov.justice.hmpps.prison.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.WorkflowFolderResponse
import uk.gov.justice.hmpps.prison.api.model.WorkflowScreenResponse
import uk.gov.justice.hmpps.prison.repository.jpa.repository.WorkflowFolderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.WorkflowScreenRepository

@Service
@Transactional(readOnly = true)
class WorkflowConfigurationService(
  private val folderRepository: WorkflowFolderRepository,
  private val screenRepository: WorkflowScreenRepository,
  @Value("\${spring.data.web.pageable.max-page-size:5000}")
  private val maxPageSize: Int,
) {
  fun getFolders(page: Int, size: Int): Page<WorkflowFolderResponse> = folderRepository.findFolders(pageRequest(page, size))

  fun getScreens(page: Int, size: Int): Page<WorkflowScreenResponse> = screenRepository.findScreens(pageRequest(page, size))

  private fun pageRequest(page: Int, size: Int): PageRequest = PageRequest.of(page, size.coerceAtMost(maxPageSize))
}
