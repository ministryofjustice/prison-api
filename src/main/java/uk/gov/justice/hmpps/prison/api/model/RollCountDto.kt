package uk.gov.justice.hmpps.prison.api.model

import uk.gov.justice.hmpps.prison.service.support.LocationProcessor

data class RollCountDto(
  val livingUnitId: Long?,
  val locationType: String?,
  val locationCode: String?,
  val fullLocationPath: String?,
  val livingUnitDesc: String?,
  val parentLocationId: Long?,
  val parentLocationType: String?,
  val parentLocationCode: String?,
  val parentFullLocationPath: String?,
  val parentLocalName: String?,
  val bedsInUse: Int?,
  val currentlyInCell: Int?,
  val outOfLivingUnits: Int?,
  val currentlyOut: Int?,
  val operationalCapacity: Int?,
  val netVacancies: Int?,
  val maximumCapacity: Int?,
  val availablePhysical: Int?,
  val outOfOrder: Int?,
) {
  fun toRollCount(prisonId: String) = RollCount(
    this.livingUnitId,
    this.locationType,
    this.locationCode,
    LocationProcessor.stripAgencyId(this.fullLocationPath, prisonId),
    this.livingUnitDesc,
    this.parentLocationId,
    this.parentLocationType,
    this.parentLocationCode,
    LocationProcessor.stripAgencyId(this.parentFullLocationPath, prisonId),
    this.parentLocalName,
    this.bedsInUse,
    this.currentlyInCell,
    this.outOfLivingUnits,
    this.currentlyOut,
    this.operationalCapacity,
    this.netVacancies,
    this.maximumCapacity,
    this.availablePhysical,
    this.outOfOrder,
  )
}
