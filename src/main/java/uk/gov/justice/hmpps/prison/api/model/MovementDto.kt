package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
data class MovementDto(
  val offenderNo: String?,
  val createDateTime: LocalDateTime?,
  val fromAgency: String?,
  val fromAgencyDescription: String?,
  val toAgency: String?,
  val toAgencyDescription: String?,
  val fromCity: String?,
  val toCity: String?,
  val movementType: String?,
  val movementTypeDescription: String?,
  val directionCode: String?,
  val movementDate: LocalDate?,
  val movementTime: LocalTime?,
  val movementReason: String?,
  val commentText: String?,
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
