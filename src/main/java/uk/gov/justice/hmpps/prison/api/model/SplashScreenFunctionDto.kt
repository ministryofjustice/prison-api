package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Splash Screen Function")
data class SplashScreenFunctionDto(
  @Schema(description = "Function name")
  val functionName: String,

  @Schema(description = "Description of the function")
  val description: String,
)
