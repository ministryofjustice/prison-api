package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The main offence of a booking")
data class MainOffence(
  @Schema(description = "Offence code, from OFFENDER_CHARGES", example = "RR84070")
  val offenceCode: String?,

  @Schema(description = "Description of the offence", example = "Actual bodily harm")
  val offenceDescription: String?,
)
