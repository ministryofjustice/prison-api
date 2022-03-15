package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime
data class ReleaseEventDto(
  var offenderNo: String? = null,
  var createDateTime: LocalDateTime? = null,
  var eventId: Long? = null,
  var fromAgency: String? = null,
  var fromAgencyDescription: String? = null,
  var releaseDate: LocalDate? = null,
  var approvedReleaseDate: LocalDate? = null,
  var eventClass: String? = null,
  var eventStatus: String? = null,
  var movementTypeCode: String? = null,
  var movementTypeDescription: String? = null,
  var movementReasonCode: String? = null,
  var movementReasonDescription: String? = null,
  var commentText: String? = null,
  var bookingActiveFlag: Boolean = false,
  var bookingInOutStatus: String? = null,
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
