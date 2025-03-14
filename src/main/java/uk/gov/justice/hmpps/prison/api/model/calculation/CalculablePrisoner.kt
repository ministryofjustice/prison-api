package uk.gov.justice.hmpps.prison.api.model.calculation

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The prisoner to be calculated as part of a bulk calculation")
data class CalculablePrisoner(
  @Schema(description = "Prisoner Identifier", example = "A1234AA", requiredMode = Schema.RequiredMode.REQUIRED)
  var prisonerNumber: String,
  @Schema(description = "The booking ID")
  val bookingId: Long,
)
