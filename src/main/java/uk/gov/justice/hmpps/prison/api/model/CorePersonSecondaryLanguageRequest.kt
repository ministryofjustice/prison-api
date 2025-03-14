package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Secondary language request. Used to add or update secondary language information.")
data class CorePersonSecondaryLanguageRequest(
  @Schema(description = "Language reference code")
  val language: String,

  @Schema(description = "Reading proficiency")
  val canRead: Boolean,

  @Schema(description = "Writing proficiency")
  val canWrite: Boolean,

  @Schema(description = "Speaking proficiency")
  val canSpeak: Boolean,
)
