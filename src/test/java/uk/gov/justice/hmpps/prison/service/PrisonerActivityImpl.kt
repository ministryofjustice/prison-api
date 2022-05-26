package uk.gov.justice.hmpps.prison.service

import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivity
import java.time.LocalDateTime

data class PrisonerActivityImpl(
  override val bookingId: Long,
  override val suspended: String?,
  override val startTime: LocalDateTime,
) : PrisonerActivity
