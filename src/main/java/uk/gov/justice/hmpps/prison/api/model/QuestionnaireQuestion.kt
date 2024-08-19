package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDate

@Schema(description = "Questionnaire Question")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class QuestionnaireQuestion(
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val questionnaireQueId: Long,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val questionSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val questionDesc: String,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val questionListSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val questionActiveFlag: Boolean = true,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val questionExpiryDate: LocalDate? = null,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val multipleAnswerFlag: Boolean = false,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "")
  private val answers: List<QuestionnaireAnswer>,
)
