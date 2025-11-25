package uk.gov.justice.hmpps.prison.api.model.questionnaire

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire

data class UpdateIncidentTypeConfigurationRequest(
  @Schema(description = "Update incident type description (optional)", example = "Drone Sighting")
  val incidentTypeDescription: String? = null,

  @Schema(description = "Update active flag (optional)")
  val active: Boolean? = null,

  @Schema(description = "Replace roles (optional)")
  val prisonerRoles: List<PrisonerRoleRequest>? = null,

  @Schema(description = "Replace questions (optional)")
  val questions: List<QuestionRequest>? = null,
) {
  fun mapQuestions(questionnaire: Questionnaire) = questions?.mapIndexed { index, q -> q.toEntity(questionnaire, index + 1) }

  fun mapRoles(questionnaire: Questionnaire) = prisonerRoles?.mapIndexed { index, r -> r.toEntity(questionnaire, index + 1) }
}
