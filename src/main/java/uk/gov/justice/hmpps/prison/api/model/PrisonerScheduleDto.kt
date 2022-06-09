package uk.gov.justice.hmpps.prison.api.model

import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class PrisonerScheduleDto(
  val offenderNo: String?,
  val eventId: Long?,
  val bookingId: Long?,
  val locationId: Long?,
  val firstName: String?,
  val lastName: String?,
  val cellLocation: String?,
  val event: String?,
  val eventType: String?,
  val eventDescription: String?,
  val eventLocation: String?,
  val eventLocationId: Long?,
  val eventStatus: String?,
  val comment: String?,
  val startTime: LocalDateTime?,
  val endTime: LocalDateTime?,
  val eventOutcome: String?,
  val performance: String?,
  val outcomeComment: String?,
  val paid: Boolean?,
  val payRate: BigDecimal?,
  val excluded: Boolean?,
  val timeSlot: TimeSlot?,
  val locationCode: String?,
  val suspended: Boolean?,
  val programStatus: String?,
  val programEndDate: LocalDate?,
  val scheduleDate: LocalDate?,
) {
  fun toPrisonerSchedule() = PrisonerSchedule(
    this.offenderNo,
    this.eventId,
    this.bookingId,
    this.locationId,
    this.firstName,
    this.lastName,
    this.cellLocation,
    this.event,
    this.eventType,
    this.eventDescription,
    this.eventLocation,
    this.eventLocationId,
    this.eventStatus,
    this.comment,
    this.startTime,
    this.endTime,
    this.eventOutcome,
    this.performance,
    this.outcomeComment,
    this.paid,
    this.payRate,
    this.excluded,
    this.timeSlot,
    this.locationCode,
    this.suspended,
  )

  fun programHasntEnded(): Boolean =
    // SQL ensures that the activity happens after the start date parameter, now need to check that the offender
    // program hasn't ended. END indicates that the program has ended, in which case the end date will also be populated
    programStatus != "END" || programEndDate == null || scheduleDate == null || programEndDate >= scheduleDate
}
