package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner sexual orientation")
data class UpdateSexualOrientation(
  @Schema(description = "The sexual orientation code", example = "HET", required = true)
  val sexualOrientation: String?,
)
