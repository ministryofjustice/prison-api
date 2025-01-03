package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner nationality")
data class UpdateNationality(
  @Schema(description = "Nationality", example = "British", required = true, nullable = true)
  val nationality: String?,
)
