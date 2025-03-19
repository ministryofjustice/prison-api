package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Secondary language request. Used to add or update secondary language information.")
data class CorePersonSecondaryLanguageRequest(
  @Schema(description = "Language reference code. Uses the `LANG` reference data domain.", example = "ENG")
  val language: String,

  @Schema(description = "Reading proficiency", example = "true")
  val canRead: Boolean,

  @Schema(description = "Writing proficiency", example = "true")
  val canWrite: Boolean,

  @Schema(description = "Speaking proficiency", example = "true")
  val canSpeak: Boolean,
)
