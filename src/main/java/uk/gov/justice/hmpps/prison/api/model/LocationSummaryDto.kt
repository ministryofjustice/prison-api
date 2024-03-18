package uk.gov.justice.hmpps.prison.api.model

data class LocationSummaryDto(
  private val locationId: Long,
  private val userDescription: String?,
  private val description: String,
  private val agencyId: String?,
) {
  fun toLocationSummary() = LocationSummary(
    this.locationId,
    this.userDescription,
    this.description,
    this.agencyId,
  )
}
