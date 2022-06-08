package uk.gov.justice.hmpps.prison.repository.jpa.repository

import java.time.LocalDate
import java.time.LocalDateTime

interface PrisonerActivity {
  val bookingId: Long
  val startTime: LocalDateTime
  val programStatus: String?
  val programEndDate: LocalDate?
  val scheduleDate: LocalDate?
  val suspended: String?
}

data class PrisonerActivitiesCount(val total: Long, val suspended: Long, val notRecorded: Long)
