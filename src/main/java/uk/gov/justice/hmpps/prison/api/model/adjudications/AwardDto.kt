package uk.gov.justice.hmpps.prison.api.model.adjudications
import java.math.BigDecimal
import java.time.LocalDate

data class AwardDto(
  val bookingId: Long?,
  val sanctionCode: String?,
  val sanctionCodeDescription: String?,
  val months: Int?,
  val days: Int?,
  val limit: BigDecimal?,
  val comment: String?,
  val effectiveDate: LocalDate?,
  val status: String?,
  val statusDescription: String?,
  val hearingId: Long?,
  val hearingSequence: Int?,
) {
  fun toAward() = Award(
    this.bookingId,
    this.sanctionCode,
    this.sanctionCodeDescription,
    this.months,
    this.days,
    this.limit,
    this.comment,
    this.effectiveDate,
    this.status,
    this.statusDescription,
    this.hearingId,
    this.hearingSequence,
  )
}
