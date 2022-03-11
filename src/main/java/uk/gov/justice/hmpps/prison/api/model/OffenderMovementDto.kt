package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalTime

data class OffenderMovementDto(
  var offenderNo: String? = null,
  var bookingId: Long? = null,
  var dateOfBirth: LocalDate? = null,
  var firstName: String? = null,
  var middleName: String? = null,
  var lastName: String? = null,
  var fromAgency: String? = null,
  var fromAgencyDescription: String? = null,
  var toAgency: String? = null,
  var toAgencyDescription: String? = null,
  var movementType: String? = null,
  var movementTypeDescription: String? = null,
  var movementReason: String? = null,
  var movementReasonDescription: String? = null,
  var directionCode: String? = null,
  var movementTime: LocalTime? = null,
  var movementDate: LocalDate? = null,
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
