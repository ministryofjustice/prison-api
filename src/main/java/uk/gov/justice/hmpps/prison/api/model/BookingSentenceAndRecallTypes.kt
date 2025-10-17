package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A booking and it's sentence and recall type")
data class BookingSentenceAndRecallTypes(
  @Schema(description = "Offender booking id.", example = "1132400")
  val bookingId: Long,

  @Schema(description = "The sentence and recall type for this booking.")
  val sentenceTypeRecallTypes: List<SentenceTypeRecallType>,
)
