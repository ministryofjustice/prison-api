package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Language Preferences Request. Used to create or update language preferences.")
class CorePersonLanguagePreferencesRequest(
  @Schema(description = "Preferred spoken language code", example = "ENG")
  val preferredSpokenLanguageCode: String? = null,

  @Schema(description = "Preferred written language code", example = "ENG")
  val preferredWrittenLanguageCode: String? = null,

  @Schema(description = "Is interpreter required", example = "true")
  val interpreterRequired: Boolean,
)
