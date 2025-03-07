package uk.gov.justice.hmpps.prison.api.model.calculation

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The active sentence envelope is a prisoner to be calculated as part of a bulk calculation")
data class CalculableSentenceEnvelopeVersion2(
  @Schema(description = "Prisoner Identifier", example = "A1234AA", requiredMode = Schema.RequiredMode.REQUIRED)
  var prisonerNumber: String,
  @Schema(description = "The booking ID")
  val bookingId: Long,
)
