package uk.gov.justice.hmpps.prison.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire

@Repository
interface QuestionnaireRepository :
  JpaRepository<Questionnaire, Long>,
  JpaSpecificationExecutor<Questionnaire> {
  fun findAllByCategory(category: String): List<Questionnaire>
  fun findOneByCategoryAndCode(category: String, code: String): Questionnaire?
}
