package uk.gov.justice.hmpps.prison.service

import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivity
import java.time.LocalDate
import java.time.LocalDateTime

data class PrisonerActivityImpl(
  override val bookingId: Long,
  override val suspended: String?,
  override val programStatus: String?,
  val programEnd: String?,
  val schedule: String?,
  val start: String,
) : PrisonerActivity {
  override val programEndDate: LocalDate? = programEnd?.let { LocalDate.parse(programEnd) }
  override val scheduleDate: LocalDate? = schedule?.let { LocalDate.parse(schedule) }
  override val startTime: LocalDateTime = start.let { LocalDateTime.parse(start) }
}
