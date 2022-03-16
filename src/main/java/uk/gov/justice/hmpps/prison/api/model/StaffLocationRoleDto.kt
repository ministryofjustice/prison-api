package uk.gov.justice.hmpps.prison.api.model
import java.math.BigDecimal
import java.time.LocalDate

data class StaffLocationRoleDto(
  val staffId: Long?,
  val firstName: String?,
  val lastName: String?,
  val status: String?,
  val thumbnailId: Long?,
  val gender: String?,
  val dateOfBirth: LocalDate?,
  val agencyId: String?,
  val agencyDescription: String?,
  val fromDate: LocalDate?,
  val toDate: LocalDate?,
  val position: String?,
  val positionDescription: String?,
  val role: String?,
  val roleDescription: String?,
  val scheduleType: String?,
  val scheduleTypeDescription: String?,
  val hoursPerWeek: BigDecimal?,
) {
  fun toStaffLocationRole() = StaffLocationRole(
    this.staffId,
    this.firstName,
    this.lastName,
    this.status,
    this.thumbnailId,
    this.gender,
    this.dateOfBirth,
    this.agencyId,
    this.agencyDescription,
    this.fromDate,
    this.toDate,
    this.position,
    this.positionDescription,
    this.role,
    this.roleDescription,
    this.scheduleType,
    this.scheduleTypeDescription,
    this.hoursPerWeek,
  )
}
