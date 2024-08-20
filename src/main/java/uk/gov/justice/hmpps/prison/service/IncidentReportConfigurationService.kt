package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeConfiguration
import uk.gov.justice.hmpps.prison.repository.QuestionnaireRepository

@Service
@Transactional(readOnly = true)
class IncidentReportConfigurationService(
  private val questionnaireRepository: QuestionnaireRepository,
) {
  fun getIncidentTypeConfiguration(
    incidentType: String? = null,
  ): List<IncidentTypeConfiguration> {
    if (incidentType != null) {
      val questionnaire = questionnaireRepository.findOneByCategoryAndCode("IR_TYPE", incidentType) ?: throw EntityNotFoundException.withId(incidentType)
      return listOf(questionnaire.toIncidentTypeConfiguration())
    }

    return questionnaireRepository.findAllByCategory("IR_TYPE").sortedBy { it.listSequence }.map { it.toIncidentTypeConfiguration() }
  }
}
