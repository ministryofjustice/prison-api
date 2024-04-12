package uk.gov.justice.hmpps.prison.api.model

import uk.gov.justice.hmpps.prison.service.support.LocationProcessor

data class RollCountDto(
  val livingUnitId: Long?,
  val fullLocationPath: String?,
  val locationPath: String?,
  val livingUnitDesc: String?,
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
    this.fullLocationPath,
    LocationProcessor.stripAgencyId(this.fullLocationPath, prisonId),
    this.livingUnitDesc,
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
