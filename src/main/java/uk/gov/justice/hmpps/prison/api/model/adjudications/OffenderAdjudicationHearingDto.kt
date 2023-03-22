package uk.gov.justice.hmpps.prison.api.model.adjudications

import java.time.LocalDateTime

data class OffenderAdjudicationHearingDto(
  val agencyId: String?,
  val offenderNo: String?,
  val hearingId: Long?,
  val hearingType: String?,
  val startTime: LocalDateTime?,
  val internalLocationId: Long?,
  val internalLocationDescription: String?,
  val eventStatus: String?,
) {
  fun toOffenderAdjudicationHearing(): OffenderAdjudicationHearing =
    OffenderAdjudicationHearing(
      agencyId,
      offenderNo,
      hearingId!!,
      hearingType,
      startTime,
      internalLocationId!!,
      internalLocationDescription,
      eventStatus,
    )
}
