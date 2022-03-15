package uk.gov.justice.hmpps.prison.api.model
import java.math.BigDecimal
import java.time.LocalDate

data class StaffLocationRoleDto(
  var staffId: Long? = null,
  var firstName: String? = null,
  var lastName: String? = null,
  var status: String? = null,
  var thumbnailId: Long? = null,
  var gender: String? = null,
  var dateOfBirth: LocalDate? = null,
  var agencyId: String? = null,
  var agencyDescription: String? = null,
  var fromDate: LocalDate? = null,
  var toDate: LocalDate? = null,
  var position: String? = null,
  var positionDescription: String? = null,
  var role: String? = null,
  var roleDescription: String? = null,
  var scheduleType: String? = null,
  var scheduleTypeDescription: String? = null,
  var hoursPerWeek: BigDecimal? = null,
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
