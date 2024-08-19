package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDate


@Schema(description = "Questionnaire Prisoner Role")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class QuestionnairePrisonerRole (
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Role type for this question set")
  val prisonerRole: String,
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "If a single party can have this role in the question set")
  val singleRole: Boolean = false,
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Indicates this role is active")
  val active: Boolean = true,
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Indicates the date the role was made inactive")
  private val expiryDate: LocalDate? = null,
)
