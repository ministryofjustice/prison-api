package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reference code and description")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RefCodeAndDescription(
  @Schema(description = "Reference code", example = "code")
  val code: String,
  @Schema(description = "Reference description", example = "description")
  val description: String,
)
