package uk.gov.justice.hmpps.prison.api.model.adjudications

import java.time.LocalDateTime

data class HearingDto(
  val oicHearingId: Long?,
  val hearingType: String?,
  val hearingTime: LocalDateTime?,
  val establishment: String?,
  val location: String?,
  val internalLocationId: Long?,
  val heardByFirstName: String?,
  val heardByLastName: String?,
  val otherRepresentatives: String?,
  val comment: String?,
) {
  fun toHearing() = Hearing(
    this.oicHearingId,
    this.hearingType,
    this.hearingTime,
    this.establishment,
    this.location,
    this.internalLocationId,
    this.heardByFirstName,
    this.heardByLastName,
    this.otherRepresentatives,
    this.comment,
    null,
  )
}
