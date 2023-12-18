package uk.gov.justice.hmpps.prison.api.model

import uk.gov.justice.hmpps.prison.service.support.LocationProcessor
import java.time.LocalDateTime

data class PrisonerPrisonScheduleDto(
  val offenderNo: String?,
  val firstName: String?,
  val lastName: String?,
  val event: String?,
  val eventType: String?,
  val eventDescription: String?,
  val eventLocation: String?,
  val eventStatus: String?,
  val comment: String?,
  val startTime: LocalDateTime?,
  val endTime: LocalDateTime?,
) {
  fun toPrisonerPrisonSchedule() = PrisonerPrisonSchedule(
    this.offenderNo,
    this.firstName,
    this.lastName,
    this.event,
    this.eventType,
    this.eventDescription,
    LocationProcessor.formatLocation(this.eventLocation),
    this.eventStatus,
    this.comment,
    this.startTime,
    this.endTime,
  )
}
