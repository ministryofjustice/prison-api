package uk.gov.justice.hmpps.prison.api.model

data class RollCountDto(
  var livingUnitId: Long? = null,
  var livingUnitDesc: String? = null,
  var bedsInUse: Int? = null,
  var currentlyInCell: Int? = null,
  var currentlyOut: Int? = null,
  var operationalCapacity: Int? = null,
  var netVacancies: Int? = null,
  var maximumCapacity: Int? = null,
  var availablePhysical: Int? = null,
  var outOfOrder: Int? = null,
) {
  fun toRollCount() = RollCount(
    this.livingUnitId,
    this.livingUnitDesc,
    this.bedsInUse,
    this.currentlyInCell,
    this.currentlyOut,
    this.operationalCapacity,
    this.netVacancies,
    this.maximumCapacity,
    this.availablePhysical,
    this.outOfOrder,
  )
}
