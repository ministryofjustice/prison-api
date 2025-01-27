package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Prisoner military records with details about service in the UK Armed Forces.")
data class MilitaryRecords(
  @Schema(description = "List of military records.")
  val militaryRecords: List<MilitaryRecord>,
)
