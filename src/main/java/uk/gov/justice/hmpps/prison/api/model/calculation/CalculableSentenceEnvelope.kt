package uk.gov.justice.hmpps.prison.api.model.calculation

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment
import uk.gov.justice.hmpps.prison.api.model.FixedTermRecallDetails
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentValues
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates

@Schema(description = "The active sentence envelope is a combination of the person information, the active booking and calculable sentences at a particular establishment")
data class CalculableSentenceEnvelope(
  @Schema(description = "The identifiers of a person necessary for a calculation")
  val person: Person,
  @Schema(description = "The booking ID")
  val bookingId: Long,
  @Schema(description = "Sentence and offence details  for a prisoner")
  val sentenceAndOffences: List<OffenderSentenceAndOffences> = listOf(),

  @Schema(description = "Adjustments at a sentence level")
  val sentenceAdjustments: List<SentenceAdjustmentValues> = listOf(),

  @Schema(description = "Adjustments at a booking level")
  val bookingAdjustments: List<BookingAdjustment> = listOf(),

  @Schema(description = "List of offender fine payments")
  val offenderFinePayments: List<OffenderFinePaymentDto> = listOf(),

  @Schema(description = "Fixed term recall details")
  val fixedTermRecallDetails: FixedTermRecallDetails? = null,

  @Schema(description = "The current set of sentence dates determined by NOMIS or recorded via overrides")
  val sentenceCalcDates: SentenceCalcDates? = null,

  )
