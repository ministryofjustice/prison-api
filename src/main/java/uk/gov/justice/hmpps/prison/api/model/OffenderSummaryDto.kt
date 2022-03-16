package uk.gov.justice.hmpps.prison.api.model

data class OffenderSummaryDto(
  val bookingId: Long?,
  val offenderNo: String?,
  val title: String?,
  val suffix: String?,
  val firstName: String?,
  val middleNames: String?,
  val lastName: String?,
  val currentlyInPrison: String?,
  val agencyLocationId: String?,
  val agencyLocationDesc: String?,
  val internalLocationId: String?,
  val internalLocationDesc: String?,
) {
  fun toOffenderSummary() = OffenderSummary(
    this.bookingId,
    this.offenderNo,
    this.title,
    this.suffix,
    this.firstName,
    this.middleNames,
    this.lastName,
    this.currentlyInPrison,
    this.agencyLocationId,
    this.agencyLocationDesc,
    this.internalLocationId,
    this.internalLocationDesc,
  )
}
