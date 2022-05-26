package uk.gov.justice.hmpps.prison.repository.jpa.repository

import java.time.LocalDateTime

interface PrisonerActivity {
  val bookingId: Long
  val startTime: LocalDateTime
  val suspended: String?
}

data class PrisonerActivitiesCount(val total: Long, val suspended: Long, val notRecorded: Long)
