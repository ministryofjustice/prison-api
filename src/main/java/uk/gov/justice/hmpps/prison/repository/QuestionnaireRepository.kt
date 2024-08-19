package uk.gov.justice.hmpps.prison.repository

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire

@Repository
interface QuestionnaireRepository : CrudRepository<Questionnaire, Long>, JpaSpecificationExecutor<Questionnaire> {
  fun findAllByCategory(category: String): List<Questionnaire>
  fun findOneByCategoryAndCode(category: String, code: String): Questionnaire
}
