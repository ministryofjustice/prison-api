package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeConfiguration
import uk.gov.justice.hmpps.prison.api.model.questionnaire.CreateIncidentTypeConfigurationRequest
import uk.gov.justice.hmpps.prison.api.model.questionnaire.UpdateIncidentTypeConfigurationRequest
import uk.gov.justice.hmpps.prison.repository.QuestionnaireRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class IncidentReportConfigurationService(
  private val questionnaireRepository: QuestionnaireRepository,
) {
  fun getIncidentTypeConfiguration(
    incidentType: String? = null,
  ): List<IncidentTypeConfiguration> {
    if (incidentType != null) {
      val questionnaire = questionnaireRepository.findOneByCategoryAndCode("IR_TYPE", incidentType)
        ?: throw EntityNotFoundException.withId(incidentType)
      return listOf(questionnaire.toIncidentTypeConfiguration())
    }

    return questionnaireRepository.findAllByCategory("IR_TYPE").sortedBy { it.listSequence }
      .map { it.toIncidentTypeConfiguration() }
  }

  @Transactional
  fun createIncidentTypeConfiguration(request: CreateIncidentTypeConfigurationRequest): IncidentTypeConfiguration {
    val questionnaire = questionnaireRepository.saveAndFlush(
      Questionnaire(
        code = request.incidentType,
        description = request.incidentTypeDescription,
        active = request.active,
        listSequence = 99,
        expiryDate = if (!request.active) {
          LocalDate.now()
        } else {
          null
        },
      ).apply {
        questions.addAll(request.mapQuestions(questionnaire = this))
        mapAnswers(request.questions)
        offenderRoles.addAll(request.mapRoles(questionnaire = this))
      },
    )

    return questionnaire.toIncidentTypeConfiguration()
  }

  @Transactional
  fun updateIncidentTypeConfiguration(
    incidentTypeCode: String,
    request: UpdateIncidentTypeConfigurationRequest,
  ): IncidentTypeConfiguration {
    val questionnaire =
      questionnaireRepository.findOneByCategoryAndCode("IR_TYPE", incidentTypeCode) ?: throw EntityNotFoundException(incidentTypeCode)

    // Optionally update active and description if provided
    request.active?.let {
      questionnaire.active = it
      if (!questionnaire.active) {
        questionnaire.expiryDate = LocalDate.now()
      } else {
        questionnaire.expiryDate = null
      }
    }
    request.incidentTypeDescription?.let { questionnaire.description = it }

    // Replace roles if provided
    if (request.prisonerRoles != null) {
      questionnaire.offenderRoles.clear()
      questionnaire.offenderRoles.addAll(request.mapRoles(questionnaire)!!)
    }

    // Replace questions if provided
    if (request.questions != null) {
      questionnaire.questions.clear()
      questionnaire.questions.addAll(request.mapQuestions(questionnaire)!!)
      questionnaire.mapAnswers(request.questions)
    }

    return questionnaireRepository.save(questionnaire).toIncidentTypeConfiguration()
  }
}
