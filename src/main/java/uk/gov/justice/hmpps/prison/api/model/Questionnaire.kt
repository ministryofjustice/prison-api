package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import jakarta.persistence.Column
import jakarta.persistence.Convert
import org.hibernate.type.YesNoConverter
import java.time.LocalDate


@Schema(description = "Questionnaire")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Questionnaire(
  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "Category to identify this questionnaire",
    example = "IR_TYPE",
  )
  private val questionnaireCategory: String,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "Code to identify this questionnaire",
    example = "ASSAULTS",
  )
  private val code: String,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "ID internal of this Questionnaire", example = "123412")
  private val questionnaireId: Long,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "List of Questions (with Answers) for this Questionnaire",
  )
  private val questions: List<QuestionnaireQuestion>,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "List of roles that can apply to a prisoner in this question set",
  )
  private val prisonerRoles: List<QuestionnairePrisonerRole>,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val active: Boolean = true,

  @Column(name = "EXPIRY_DATE")
  var expiryDate: LocalDate? = null,
  )
