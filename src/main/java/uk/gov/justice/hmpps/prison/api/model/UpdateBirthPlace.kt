package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner birth place (city or town of birth)")
data class UpdateBirthPlace(
  @Schema(description = "Birth place (city or town of birth)", example = "SHEFFIELD", required = true, nullable = true)
  val birthPlace: String?,
)
