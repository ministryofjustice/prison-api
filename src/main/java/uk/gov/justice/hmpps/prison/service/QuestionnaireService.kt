package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.QuestionnaireRepository
import uk.gov.justice.hmpps.prison.api.model.Questionnaire as QuestionnaireDTO

@Service
@Transactional(readOnly = true)
class QuestionnaireService(
  private val questionnaireRepository: QuestionnaireRepository,
) {
  fun getQuestionnaires(
    questionnaireCategory: String = "IR_TYPE",
    questionnaireCode: String? = null,
  ): List<QuestionnaireDTO> {
    if (questionnaireCode != null) {
      val questionnaire = questionnaireRepository.findOneByCategoryAndCode(questionnaireCategory, questionnaireCode)
      return listOf(questionnaire.toDto())
    }

    return questionnaireRepository.findAllByCategory(questionnaireCategory).sortedBy { it.listSequence }.map { it.toDto() }
  }
}
