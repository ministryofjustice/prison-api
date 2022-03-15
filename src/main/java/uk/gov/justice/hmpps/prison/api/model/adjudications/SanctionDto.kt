package uk.gov.justice.hmpps.prison.api.model.adjudications
import java.time.LocalDateTime

data class SanctionDto(
  val sanctionType: String?,
  val sanctionDays: Long?,
  val sanctionMonths: Long?,
  val compensationAmount: Long?,
  val effectiveDate: LocalDateTime?,
  val status: String?,
  val statusDate: LocalDateTime?,
  val comment: String?,
  val sanctionSeq: Long?,
  val consecutiveSanctionSeq: Long?,
  val oicHearingId: Long,
  val resultSeq: Long?,
) {
  fun toSanction() = Sanction(
    this.sanctionType,
    this.sanctionDays,
    this.sanctionMonths,
    this.compensationAmount,
    this.effectiveDate,
    this.status,
    this.statusDate,
    this.comment,
    this.sanctionSeq,
    this.consecutiveSanctionSeq,
    this.oicHearingId,
    this.resultSeq,
  )
}
