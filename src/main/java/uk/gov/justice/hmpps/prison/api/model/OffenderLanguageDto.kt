package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

data class OffenderLanguageDto(
  @Schema(description = "A LOV from domain LANG_TYPE", allowableValues = ["PRIM", "SEC", "PREF_SPEAK", "PREF_WRITE"])
  val type: String,
  @Schema(description = "The actual language code, from domain LANG", example = "ENG")
  val code: String,
  @Schema(
    description = """The level of reading skill, from domain LANG_SKILLS:
    |Y  Yes
    |A	Average
    |D	Dyslexia
    |G	Good
    |N	Nil
    |P	Poor
    |R	Refused""",
    allowableValues = ["Y", "N", "A", "D", "G", "P", "R"],
  )
  val readSkill: String?,
  @Schema(description = "The level of writing skill, see description for readSkill")
  val writeSkill: String?,
  @Schema(description = "The level of speaking skill, see description for readSkill")
  val speakSkill: String?,
  @Schema(description = "Whether interpreter requested")
  val interpreterRequested: Boolean,
)
