package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class InmateBasicDetailsDto(
  val bookingId: Long?,
  val bookingNo: String?,
  val offenderNo: String?,
  val firstName: String?,
  val middleName: String?,
  val lastName: String?,
  val agencyId: String?,
  val assignedLivingUnitId: Long?,
  val assignedLivingUnitDesc: String?,
  val dateOfBirth: LocalDate?,
) {
  fun toInmateBasicDetails() = InmateBasicDetails(
    this.bookingId,
    this.bookingNo,
    this.offenderNo,
    this.firstName,
    this.middleName,
    this.lastName,
    this.agencyId,
    this.assignedLivingUnitId,
    this.assignedLivingUnitDesc,
    this.dateOfBirth,
  )
}
