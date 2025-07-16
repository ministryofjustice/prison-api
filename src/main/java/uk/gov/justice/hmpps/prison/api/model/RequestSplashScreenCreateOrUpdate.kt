package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Splash Screen")
data class RequestSplashScreenCreateOrUpdate(

  @Schema(description = "Function name")
  val functionName: String? = null,

  @Schema(description = "Function details")
  val function: SplashScreenFunctionDto? = null,

  @Schema(description = "Warning text to display", example = "Access to this screen will soon be revoked")
  val warningText: String? = null,

  @Schema(description = "Blocked text to display when access is blocked", example = "You can not longer use this screen, use DPS")
  val blockedText: String? = null,

  @Schema(description = "Block access type", example = "NO")
  val blockAccessType: BlockAccessType = BlockAccessType.NO,

  @Schema(description = "List of conditions for this splash screen")
  val conditions: List<RequestSplashConditionUpdate> = emptyList(),
)
