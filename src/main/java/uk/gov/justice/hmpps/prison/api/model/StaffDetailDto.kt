package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class StaffDetailDto(
  val staffId: Long?,
  val firstName: String?,
  val lastName: String?,
  val status: String?,
  val thumbnailId: Long?,
  val gender: String?,
  val dateOfBirth: LocalDate?,
) {
  fun toStaffDetail() = StaffDetail(
    this.staffId,
    this.firstName,
    this.lastName,
    this.status,
    this.thumbnailId,
    this.gender,
    this.dateOfBirth,
  )
}
