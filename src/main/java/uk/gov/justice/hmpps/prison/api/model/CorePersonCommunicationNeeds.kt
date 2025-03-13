package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
@Schema(description = "Core Person Record Communication Needs")
data class CorePersonCommunicationNeeds(
  @Schema(description = "Prisoner number", example = "A1234BC")
  val prisonerNumber: String,

  @Schema(description = "Language preferences")
  val languagePreferences: CorePersonLanguagePreferences?,

  @Schema(description = "List of secondary languages")
  val secondaryLanguages: List<CorePersonSecondaryLanguage>,
)
