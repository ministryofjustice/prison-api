package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDate

@Schema(description = "Incident Type Answer")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentTypeAnswer(
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "ID for this Answer")
  val questionnaireAnsId: Long,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Sequence for this answer", example = "1")
  val answerSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Answer Text", example = "YES")
  val answerDesc: String,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Answer Sequence", example = "1")
  val answerListSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Indicates this answer is active", example = "true")
  val answerActiveFlag: Boolean = true,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Date expired (not used)", example = "2017-01-02T00:00:00")
  val answerExpiryDate: LocalDate? = null,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "Should the answer include date information?",
    example = "false",
  )
  val dateRequiredFlag: Boolean = false,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Should the answer include comment?", example = "false")
  val commentRequiredFlag: Boolean = false,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Next questionnaire question ID")
  val nextQuestionnaireQueId: Long? = null,

)
