package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class ReasonableAdjustmentDto(
  val treatmentCode: String?,
  val commentText: String?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val agencyId: String?,
  val treatmentDescription: String?,
) {
  fun toReasonableAdjustment() = ReasonableAdjustment(
    this.treatmentCode,
    this.commentText,
    this.startDate,
    this.endDate,
    this.agencyId,
    this.treatmentDescription,
  )
}
