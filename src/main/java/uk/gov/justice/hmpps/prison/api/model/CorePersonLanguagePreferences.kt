package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.LanguageReferenceCode

@JsonInclude(NON_NULL)
@Schema(description = "Core Person Record Language Preferences")
data class CorePersonLanguagePreferences(
  @Schema(description = "Preferred spoken language")
  val preferredSpokenLanguage: LanguageReferenceCode? = null,

  @Schema(description = "Preferred written language")
  val preferredWrittenLanguage: LanguageReferenceCode? = null,

  @Schema(description = "Is interpreter required", example = "true")
  val interpreterRequired: Boolean? = false,
)
