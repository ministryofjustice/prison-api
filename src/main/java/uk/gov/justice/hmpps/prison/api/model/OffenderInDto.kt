package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
data class OffenderInDto(
  val offenderNo: String?,
  val bookingId: Long?,
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val middleName: String?,
  val lastName: String?,
  val fromAgencyId: String?,
  val fromAgencyDescription: String?,
  val toAgencyId: String?,
  val toAgencyDescription: String?,
  val fromCity: String?,
  val toCity: String?,
  val movementTime: LocalTime?,
  val movementDateTime: LocalDateTime?,
  val location: String?,
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
