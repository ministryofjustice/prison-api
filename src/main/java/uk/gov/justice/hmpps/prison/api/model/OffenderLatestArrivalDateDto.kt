package uk.gov.justice.hmpps.prison.api.model

import java.time.LocalDate

data class OffenderLatestArrivalDateDto(
  val offenderNo: String,
  val latestArrivalDate: LocalDate,
) {
  fun toOffenderLatestArrivalDate() = OffenderLatestArrivalDate(offenderNo, latestArrivalDate)
}
