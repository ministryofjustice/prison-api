package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner religion")
data class UpdateReligion(
  @Schema(description = "Nationality", example = "ZORO", required = true, nullable = true)
  val religion: String?,
)
