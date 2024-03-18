package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Offender Sentence Calculation Summary")
data class SentenceCalculationSummary(
  val bookingId: Long,
  val offenderNo: String,
  val firstName: String?,
  val lastName: String,
  val agencyLocationId: String,
  val agencyDescription: String,
  val offenderSentCalculationId: Long,
  val calculationDate: LocalDateTime,
  val staffId: Long?,
  val commentText: String? = null,
  val calculationReason: String,
  val calculatedByUserId: String,
)
