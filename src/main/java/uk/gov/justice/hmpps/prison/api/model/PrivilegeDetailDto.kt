package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime

data class PrivilegeDetailDto(
  val bookingId: Long?,
  val sequence: Long?,
  val iepDate: LocalDate?,
  val iepTime: LocalDateTime?,
  val agencyId: String?,
  val iepLevel: String?,
  val comments: String?,
  val userId: String?,
  val auditModuleName: String?,
) {
  fun toPrivilegeDetail() = PrivilegeDetail(
    this.bookingId,
    this.sequence,
    this.iepDate,
    this.iepTime,
    this.agencyId,
    this.iepLevel,
    this.comments,
    this.userId,
    this.auditModuleName,
  )
}
