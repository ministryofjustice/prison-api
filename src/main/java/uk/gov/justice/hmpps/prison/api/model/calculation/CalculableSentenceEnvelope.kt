package uk.gov.justice.hmpps.prison.api.model.calculation

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment
import uk.gov.justice.hmpps.prison.api.model.FixedTermRecallDetails
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentValues
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary

@Schema(description = "The active sentence envelope is a combination of the person information, the active booking and calculable sentences at a particular establishment")
data class CalculableSentenceEnvelope(
  @Schema(description = "The identifiers of a person necessary for a calculation")
  val person: Person,

  @Schema(description = "Most recent term in prison")
  val latestPrisonTerm: SentenceSummary.PrisonTerm? = null,

  @Schema(description = "Adjustments at a sentence level")
  val sentenceAdjustments: List<SentenceAdjustmentValues> = listOf(),

  @Schema(description = "Adjustments at a booking level")
  val bookingAdjustments: List<BookingAdjustment> = listOf(),

  @Schema(description = "List of offender fine payments")
  val offenderFinePayments: List<OffenderFinePaymentDto> = listOf(),

  @Schema(description = "Fixed term recall details")
  val fixedTermRecallDetails: FixedTermRecallDetails? = null,
)
