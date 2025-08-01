package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
@Schema(description = "Core Person Record Language Preferences")
data class CorePersonLanguagePreferences(
  @Schema(description = "Preferred spoken language")
  val preferredSpokenLanguage: ReferenceDataValue? = null,

  @Schema(description = "Preferred written language")
  val preferredWrittenLanguage: ReferenceDataValue? = null,

  @Schema(description = "Is interpreter required", example = "true")
  val interpreterRequired: Boolean? = null,
)
