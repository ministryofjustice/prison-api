package uk.gov.justice.hmpps.prison.api.model
import uk.gov.justice.hmpps.prison.api.support.CategorisationStatus
import java.time.LocalDate

data class OffenderCategoriseDto(
  val offenderNo: String?,
  val bookingId: Long?,
  val firstName: String?,
  val lastName: String?,
  val assessmentDate: LocalDate?,
  val approvalDate: LocalDate?,
  val assessmentSeq: Int?,
  val assessmentTypeId: Long?,
  val assessStatus: String?,
  val categoriserFirstName: String?,
  val categoriserLastName: String?,
  val approverFirstName: String?,
  val approverLastName: String?,
  val category: String?,
  val nextReviewDate: LocalDate?,
  val status: CategorisationStatus?,
) {
  fun toOffenderCategorise() = OffenderCategorise(
    this.offenderNo,
    this.bookingId,
    this.firstName,
    this.lastName,
    this.assessmentDate,
    this.approvalDate,
    this.assessmentSeq,
    this.assessmentTypeId,
    this.assessStatus,
    this.categoriserFirstName,
    this.categoriserLastName,
    this.approverFirstName,
    this.approverLastName,
    this.category,
    this.nextReviewDate,
    this.status,
  )
}
