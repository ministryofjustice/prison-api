package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class OffenderKeyWorkerDto(
  val offenderNo: String?,
  val staffId: Long?,
  val agencyId: String?,
  val assigned: LocalDateTime?,
  val expired: LocalDateTime?,
  val userId: String?,
  val active: String?,
  val created: LocalDateTime?,
  val createdBy: String?,
  val modified: LocalDateTime?,
  val modifiedBy: String?,
) {
  fun toOffenderKeyWorker() = OffenderKeyWorker(
    this.offenderNo,
    this.staffId,
    this.agencyId,
    this.assigned,
    this.expired,
    this.userId,
    this.active,
    this.created,
    this.createdBy,
    this.modified,
    this.modifiedBy,
  )
}
