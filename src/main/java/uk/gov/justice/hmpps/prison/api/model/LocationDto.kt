package uk.gov.justice.hmpps.prison.api.model

data class LocationDto(
  val locationId: Long?,
  val locationType: String?,
  val description: String?,
  val locationUsage: String?,
  val agencyId: String?,
  val parentLocationId: Long?,
  val currentOccupancy: Int?,
  val locationPrefix: String?,
  val operationalCapacity: Int?,
  val userDescription: String?,
  val internalLocationCode: String?,
  val subLocations: Boolean?,
) {
  fun toLocation() = Location(
    this.locationId,
    this.locationType,
    this.description,
    this.locationUsage,
    this.agencyId,
    this.parentLocationId,
    this.currentOccupancy,
    this.locationPrefix,
    this.operationalCapacity,
    this.userDescription,
    this.internalLocationCode,
    this.subLocations,
  )
}
