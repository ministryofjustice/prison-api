package uk.gov.justice.hmpps.prison.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CourtEventDto(
  var offenderNo: String? = null,
  var createDateTime: LocalDateTime? = null,
  var eventId: Long? = null,
  var fromAgency: String? = null,
  var fromAgencyDescription: String? = null,
  var toAgency: String? = null,
  var toAgencyDescription: String? = null,
  var eventDate: LocalDate? = null,
  var startTime: LocalDateTime? = null,
  var endTime: LocalDateTime? = null,
  var eventClass: String? = null,
  var eventType: String? = null,
  var eventSubType: String? = null,
  var eventStatus: String? = null,
  var judgeName: String? = null,
  var directionCode: String? = null,
  var commentText: String? = null,
  var bookingActiveFlag: Boolean = false,
  var bookingInOutStatus: String? = null,
) {
  fun toCourtEvent() = CourtEvent(
    this.offenderNo,
    this.createDateTime,
    this.eventId,
    this.fromAgency,
    this.fromAgencyDescription,
    this.toAgency,
    this.toAgencyDescription,
    this.eventDate,
    this.startTime,
    this.endTime,
    this.eventClass,
    this.eventType,
    this.eventSubType,
    this.eventStatus,
    this.judgeName,
    this.directionCode,
    this.commentText,
    this.bookingActiveFlag,
    this.bookingInOutStatus,
  )
}
