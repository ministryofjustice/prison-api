package uk.gov.justice.hmpps.prison.api.model.questionnaire

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.Questionnaire
import uk.gov.justice.hmpps.prison.repository.jpa.model.QuestionnaireOffenderRole
import uk.gov.justice.hmpps.prison.repository.jpa.model.QuestionnaireOffenderRoleId
import java.time.LocalDate

data class PrisonerRoleRequest(
  @Schema(description = "Prisoner role code", example = "PERPETRATOR", required = true)
  val prisonerRole: String,

  @Schema(description = "Indicates only a single role can be assigned", example = "false", defaultValue = "false")
  val singleRole: Boolean = false,

  @Schema(description = "Active flag for role", example = "true")
  val active: Boolean = true,
) {
  fun toEntity(questionnaire: Questionnaire, index: Int) = QuestionnaireOffenderRole(
    id = QuestionnaireOffenderRoleId(
      questionnaireId = questionnaire.id,
      offenderRole = prisonerRole,
    ),
    singleRole = singleRole,
    active = active,
    listSequence = index,
    expiryDate = if (!active) {
      LocalDate.now()
    } else {
      null
    },
  )
}
