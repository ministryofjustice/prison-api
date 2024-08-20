package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDate

@Schema(description = "Incident type question")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentTypeQuestion(
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Question ID")
  val questionnaireQueId: Long,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Question sequence number")
  val questionSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Question description")
  val questionDesc: String,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Question list sequence")
  val questionListSeq: Int = 0,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Indicates question is active")
  val questionActiveFlag: Boolean = true,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Date question was retired")
  val questionExpiryDate: LocalDate? = null,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Indicate multiple responses can be given")
  val multipleAnswerFlag: Boolean = false,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Order list of answers")
  val answers: List<IncidentTypeAnswer>,
)
