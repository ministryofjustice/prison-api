package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
data class OffenderInDto(
  var offenderNo: String? = null,
  var bookingId: Long? = null,
  var dateOfBirth: LocalDate? = null,
  var firstName: String? = null,
  var middleName: String? = null,
  var lastName: String? = null,
  var fromAgencyId: String? = null,
  var fromAgencyDescription: String? = null,
  var toAgencyId: String? = null,
  var toAgencyDescription: String? = null,
  var fromCity: String? = null,
  var toCity: String? = null,
  var movementTime: LocalTime? = null,
  var movementDateTime: LocalDateTime? = null,
  var location: String? = null,
) {
  fun toOffenderIn() = OffenderIn(
    this.offenderNo,
    this.bookingId,
    this.dateOfBirth,
    this.firstName,
    this.middleName,
    this.lastName,
    this.fromAgencyId,
    this.fromAgencyDescription,
    this.toAgencyId,
    this.toAgencyDescription,
    this.fromCity,
    this.toCity,
    this.movementTime,
    this.movementDateTime,
    this.location,
  )
}
