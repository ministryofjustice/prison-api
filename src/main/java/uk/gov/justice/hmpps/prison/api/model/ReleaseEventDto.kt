package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime
data class ReleaseEventDto(
  val offenderNo: String?,
  val createDateTime: LocalDateTime?,
  val eventId: Long?,
  val fromAgency: String?,
  val fromAgencyDescription: String?,
  val releaseDate: LocalDate?,
  val approvedReleaseDate: LocalDate?,
  val eventClass: String?,
  val eventStatus: String?,
  val movementTypeCode: String?,
  val movementTypeDescription: String?,
  val movementReasonCode: String?,
  val movementReasonDescription: String?,
  val commentText: String?,
  val bookingActiveFlag: Boolean,
  val bookingInOutStatus: String?,
) {
  fun toReleaseEvent() = ReleaseEvent(
    this.offenderNo,
    this.createDateTime,
    this.eventId,
    this.fromAgency,
    this.fromAgencyDescription,
    this.releaseDate,
    this.approvedReleaseDate,
    this.eventClass,
    this.eventStatus,
    this.movementTypeCode,
    this.movementTypeDescription,
    this.movementReasonCode,
    this.movementReasonDescription,
    this.commentText,
    this.bookingActiveFlag,
    this.bookingInOutStatus,
  )
}
