package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalTime

data class OffenderMovementDto(
  val offenderNo: String?,
  val bookingId: Long?,
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val middleName: String?,
  val lastName: String?,
  val fromAgency: String?,
  val fromAgencyDescription: String?,
  val toAgency: String?,
  val toAgencyDescription: String?,
  val movementType: String?,
  val movementTypeDescription: String?,
  val movementReason: String?,
  val movementReasonDescription: String?,
  val directionCode: String?,
  val movementTime: LocalTime?,
  val movementDate: LocalDate?,
) {
  fun toOffenderMovement() = OffenderMovement(
    this.offenderNo,
    this.bookingId,
    this.dateOfBirth,
    this.firstName,
    this.middleName,
    this.lastName,
    this.fromAgency,
    this.fromAgencyDescription,
    this.toAgency,
    this.toAgencyDescription,
    this.movementType,
    this.movementTypeDescription,
    this.movementReason,
    this.movementReasonDescription,
    this.directionCode,
    this.movementTime,
    this.movementDate,
  )
}
