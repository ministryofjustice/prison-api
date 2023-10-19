package uk.gov.justice.hmpps.prison.api.model

data class RollCountDto(
  val livingUnitId: Long?,
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
  fun toRollCount() = RollCount(
    this.livingUnitId,
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
