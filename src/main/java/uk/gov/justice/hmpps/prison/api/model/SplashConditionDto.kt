package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Splash Screen Condition")
data class SplashConditionDto(
  @Schema(description = "Splash condition ID", example = "1")
  val splashConditionId: Long,

  @Schema(description = "Condition type", example = "CASELOAD")
  val conditionType: String,

  @Schema(description = "Condition value", example = "MDI")
  val conditionValue: String,

  @Schema(description = "Block access", example = "false")
  val blockAccess: Boolean = false,
)
