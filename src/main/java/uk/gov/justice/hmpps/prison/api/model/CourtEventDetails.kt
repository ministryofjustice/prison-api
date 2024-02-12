package uk.gov.justice.hmpps.prison.api.model

import java.time.LocalDateTime

data class CourtEventDetails(
  val eventId: Long,
  val startTime: LocalDateTime,
  val courtLocation: String,
  val courtEventType: String,
  val comments: String? = null,
  val caseReference: String? = null,
)
