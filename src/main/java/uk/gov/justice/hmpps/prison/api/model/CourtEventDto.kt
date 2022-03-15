package uk.gov.justice.hmpps.prison.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CourtEventDto(
  val offenderNo: String?,
  val createDateTime: LocalDateTime?,
  val eventId: Long?,
  val fromAgency: String?,
  val fromAgencyDescription: String?,
  val toAgency: String?,
  val toAgencyDescription: String?,
  val eventDate: LocalDate?,
  val startTime: LocalDateTime?,
  val endTime: LocalDateTime?,
  val eventClass: String?,
  val eventType: String?,
  val eventSubType: String?,
  val eventStatus: String?,
  val judgeName: String?,
  val directionCode: String?,
  val commentText: String?,
  val bookingActiveFlag: Boolean,
  val bookingInOutStatus: String?,
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
