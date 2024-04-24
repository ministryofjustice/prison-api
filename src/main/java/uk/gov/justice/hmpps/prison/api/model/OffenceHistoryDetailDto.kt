package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class OffenceHistoryDetailDto(
  val bookingId: Long?,
  val offenceDate: LocalDate?,
  val offenceRangeDate: LocalDate?,
  val offenceDescription: String?,
  val offenceCode: String?,
  val statuteCode: String?,
  val mostSerious: Boolean?,
  val primaryResultCode: String?,
  val secondaryResultCode: String?,
  val primaryResultDescription: String?,
  val secondaryResultDescription: String?,
  val primaryResultConviction: Boolean?,
  val secondaryResultConviction: Boolean?,
  val courtDate: LocalDate?,
  val caseId: Long?,
) {
  fun toOffenceHistoryDetail() = OffenceHistoryDetail(
    this.bookingId,
    this.offenceDate,
    this.offenceRangeDate,
    this.offenceDescription,
    this.offenceCode,
    this.statuteCode,
    this.mostSerious,
    this.primaryResultCode,
    this.secondaryResultCode,
    this.primaryResultDescription,
    this.secondaryResultDescription,
    this.primaryResultConviction,
    this.secondaryResultConviction,
    this.courtDate,
    this.caseId,
    null,
  )
}
