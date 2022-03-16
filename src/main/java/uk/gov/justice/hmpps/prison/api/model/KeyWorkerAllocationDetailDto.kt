package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class KeyWorkerAllocationDetailDto(
  val bookingId: Long?,
  val offenderNo: String?,
  val firstName: String?,
  val middleNames: String?,
  val lastName: String?,
  val staffId: Long?,
  val agencyId: String?,
  val assigned: LocalDateTime?,
  val internalLocationDesc: String?,
) {
  fun toKeyWorkerAllocationDetail() = KeyWorkerAllocationDetail(
    this.bookingId,
    this.offenderNo,
    this.firstName,
    this.middleNames,
    this.lastName,
    this.staffId,
    this.agencyId,
    this.assigned,
    this.internalLocationDesc,
  )
}
