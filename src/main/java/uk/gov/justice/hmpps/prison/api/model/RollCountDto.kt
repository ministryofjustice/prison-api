package uk.gov.justice.hmpps.prison.api.model

import uk.gov.justice.hmpps.prison.service.support.LocationProcessor

data class RollCountDto(
  val livingUnitId: Long,
  val locationType: String,
  val locationCode: String,
  val fullLocationPath: String?,
  val localName: String?,
  val certified: String,
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
    livingUnitId,
    locationType,
    locationCode,
    LocationProcessor.stripAgencyId(fullLocationPath, prisonId),
    certified == "Y",
    buildLocationDescription(locationType, locationCode, localName),
    parentLocationId,
    parentLocationType,
    parentLocationCode,
    LocationProcessor.stripAgencyId(parentFullLocationPath, prisonId),
    parentLocationId?.let { buildLocationDescription(parentLocationType!!, parentLocationCode!!, parentLocalName) },
    bedsInUse,
    currentlyInCell,
    outOfLivingUnits,
    currentlyOut,
    operationalCapacity,
    netVacancies,
    maximumCapacity,
    availablePhysical,
    outOfOrder,
  )

  private fun buildLocationDescription(type: String, code: String, localName: String? = null): String? =
    if (type == "CELL") {
      code
    } else {
      LocationProcessor.formatLocation(localName ?: code)
    }
}
