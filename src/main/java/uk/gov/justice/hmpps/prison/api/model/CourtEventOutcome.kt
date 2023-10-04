package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Court event outcome")
data class CourtEventOutcome(
  @Schema(description = "The court event identifier", example = "201206")
  val eventId: Long,

  @Schema(description = "The court event outcome reason code", example = "5500")
  val outcomeReasonCode: String,
)
