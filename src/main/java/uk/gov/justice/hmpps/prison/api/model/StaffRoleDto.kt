package uk.gov.justice.hmpps.prison.api.model

data class StaffRoleDto(
  val role: String?,
  val roleDescription: String?,
) {
  fun toStaffRole() = StaffRole(
    this.role,
    this.roleDescription,
  )
}
