package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDate

@Schema(description = "Questionnaire Answer")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class QuestionnaireAnswer(
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "ID for this Answer")
  private val questionnaireAnsId: Long,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "", example = "1")
  private val answerSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Answer Text", example = "YES")
  private val answerDesc: String,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Answer Sequence", example = "1")
  private val answerListSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "", example = "true")
  private val answerActiveFlag: Boolean = true,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "", example = "2017-01-02T00:00:00")
  private val answerExpiryDate: LocalDate? = null,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "Should the answer include date information?",
    example = "false",
  )
  private val dateRequiredFlag: Boolean = false,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Should the answer include Comment?", example = "false")
  private val commentRequiredFlag: Boolean = false,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Next questionnaire question ID")
  private val nextQuestionnaireQueId: Long? = null,

)
