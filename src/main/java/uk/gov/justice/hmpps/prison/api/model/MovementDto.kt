package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
data class MovementDto(
  var offenderNo: String? = null,
  var createDateTime: LocalDateTime? = null,
  var fromAgency: String? = null,
  var fromAgencyDescription: String? = null,
  var toAgency: String? = null,
  var toAgencyDescription: String? = null,
  var fromCity: String? = null,
  var toCity: String? = null,
  var movementType: String? = null,
  var movementTypeDescription: String? = null,
  var directionCode: String? = null,
  var movementDate: LocalDate? = null,
  var movementTime: LocalTime? = null,
  var movementReason: String? = null,
  var commentText: String? = null,
) {
  fun toMovement() = Movement(
    this.offenderNo,
    this.createDateTime,
    this.fromAgency,
    this.fromAgencyDescription,
    this.toAgency,
    this.toAgencyDescription,
    this.fromCity,
    this.toCity,
    this.movementType,
    this.movementTypeDescription,
    this.directionCode,
    this.movementDate,
    this.movementTime,
    this.movementReason,
    this.commentText,
  )
}
