package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.LanguageReferenceCode

@Schema(description = "Secondary language information")
data class CorePersonSecondaryLanguage(
  @Schema(description = "Language reference code")
  val language: LanguageReferenceCode,

  @Schema(description = "Reading proficiency")
  val canRead: Boolean,

  @Schema(description = "Writing proficiency")
  val canWrite: Boolean,

  @Schema(description = "Speaking proficiency")
  val canSpeak: Boolean,
)
