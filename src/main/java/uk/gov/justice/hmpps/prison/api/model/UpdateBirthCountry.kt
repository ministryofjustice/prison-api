package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner birth country")
data class UpdateBirthCountry(
  @Schema(description = "Country code", example = "GBR", required = true, nullable = true)
  val countryCode: String?,
)
