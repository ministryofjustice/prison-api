package uk.gov.justice.hmpps.prison.api.model.questionnaire

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire

data class CreateIncidentTypeConfigurationRequest(
  @Schema(description = "Incident type code", example = "DIRTY_PROTEST_1", required = true)
  val incidentType: String,

  @Schema(description = "Incident type description", example = "Dirty Protest", required = true)
  val incidentTypeDescription: String,

  @Schema(description = "Active flag for incident type", example = "true", defaultValue = "true")
  val active: Boolean = true,

  @Schema(description = "Questions and answers for this incident type")
  val questions: List<QuestionRequest> = emptyList(),

  @Schema(description = "Roles that can be applied to a prisoner for this incident type")
  val prisonerRoles: List<PrisonerRoleRequest> = emptyList(),

) {

  fun mapQuestions(questionnaire: Questionnaire) = questions.mapIndexed { index, q -> q.toEntity(questionnaire, index + 1) }

  fun mapRoles(questionnaire: Questionnaire) = prisonerRoles.mapIndexed { index, r -> r.toEntity(questionnaire, index + 1) }
}
