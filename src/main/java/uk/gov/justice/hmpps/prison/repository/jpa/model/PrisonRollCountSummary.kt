package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Subselect
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import uk.gov.justice.hmpps.prison.service.LocationRollCount
import uk.gov.justice.hmpps.prison.service.ResidentialLocation
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor

@Entity
@Subselect(
  """
    SELECT
            AIL.INTERNAL_LOCATION_ID                              AS LOCATION_ID,
            AIL.AGY_LOC_ID                                        AS PRISON_ID,
            AIL.INTERNAL_LOCATION_TYPE                            AS LOCATION_TYPE,
            AIL.INTERNAL_LOCATION_CODE                            AS LOCATION_CODE,
            AIL.DESCRIPTION                                       AS FULL_LOCATION_PATH,
            AIL.USER_DESC                                         AS LOCAL_NAME,
            AIL.CERTIFIED_FLAG                                    AS CERTIFIED,
            PLOC.INTERNAL_LOCATION_ID                             AS PARENT_LOCATION_ID,
            VR.BEDS_IN_USE,
            VR.CURRENTLY_IN_CELL,
            VR.OUT_OF_LIVING_UNITS,
            VR.CURRENTLY_OUT,
            AIL.OPERATION_CAPACITY                                AS OPERATIONAL_CAPACITY,
            AIL.OPERATION_CAPACITY - VR.BEDS_IN_USE               AS NET_VACANCIES,
            AIL.CAPACITY                                          AS MAXIMUM_CAPACITY,
            AIL.CAPACITY - VR.BEDS_IN_USE                         AS AVAILABLE_PHYSICAL,
            (SELECT COUNT(*)
            FROM AGENCY_INTERNAL_LOCATIONS AIL2
            INNER JOIN LIVING_UNITS_MV LU2 ON AIL2.INTERNAL_LOCATION_ID = LU2.LIVING_UNIT_ID
                    WHERE AIL2.AGY_LOC_ID = VR.AGY_LOC_ID
                    AND LU2.ROOT_LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                    AND (
                            AIL2.DEACTIVATE_REASON_CODE IS NULL OR
                                    AIL2.DEACTIVATE_REASON_CODE NOT IN ('A', 'C', 'E', 'I')
                    )
            AND sysdate BETWEEN DEACTIVATE_DATE AND COALESCE(REACTIVATE_DATE,sysdate)) AS OUT_OF_ORDER
            FROM
            (SELECT
                    LU.AGY_LOC_ID,
            LU.ROOT_LIVING_UNIT_ID,
            SUM(DECODE(OB.LIVING_UNIT_ID, NULL, 0, 1)) AS BEDS_IN_USE,
            SUM(DECODE(OB.AGENCY_IML_ID, NULL, DECODE (OB.IN_OUT_STATUS, 'IN', 1, 0), 0)) AS CURRENTLY_IN_CELL,
            SUM(DECODE(OB.AGENCY_IML_ID, NULL, 0, DECODE (OB.IN_OUT_STATUS, 'IN', 1, 0))) AS OUT_OF_LIVING_UNITS,
            SUM(DECODE(OB.IN_OUT_STATUS, 'OUT', 1, 0)) AS CURRENTLY_OUT
            FROM LIVING_UNITS_MV LU
            LEFT JOIN OFFENDER_BOOKINGS OB ON LU.LIVING_UNIT_ID = OB.LIVING_UNIT_ID AND LU.AGY_LOC_ID = OB.AGY_LOC_ID
                    GROUP BY LU.AGY_LOC_ID, LU.ROOT_LIVING_UNIT_ID
            ) VR
            INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON AIL.INTERNAL_LOCATION_ID = VR.ROOT_LIVING_UNIT_ID
            LEFT JOIN AGENCY_INTERNAL_LOCATIONS PLOC ON PLOC.INTERNAL_LOCATION_ID = AIL.PARENT_INTERNAL_LOCATION_ID 
            WHERE AIL.UNIT_TYPE IS NOT NULL
            AND AIL.ACTIVE_FLAG = 'Y'
            """,
)
@EntityOpen
class PrisonRollCountSummary(
  @Id
  val locationId: Long,
  val prisonId: String,
  val locationType: String,
  val locationCode: String,
  val fullLocationPath: String,
  val localName: String?,
  val certified: String,
  val parentLocationId: Long?,
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

  fun toDto(locations: List<PrisonRollCountSummary>, includeLeaf: Boolean = true) = ResidentialLocation(
    locationId = locationId.toString(),
    locationType = locationType,
    locationCode = locationCode,
    fullLocationPath = getPathHierarchy(),
    certified = isCertified(),
    localName = getLocalNameDescription(),
    subLocations = getSubLocations(locations = locations, includeLeaf = includeLeaf),
    rollCount = LocationRollCount(
      bedsInUse = bedsInUse ?: 0,
      currentlyInCell = currentlyInCell ?: 0,
      currentlyOut = currentlyOut ?: 0,
      workingCapacity = operationalCapacity ?: 0,
      netVacancies = netVacancies ?: 0,
      outOfOrder = outOfOrder ?: 0,
    ),
  )

  fun getSubLocations(locations: List<PrisonRollCountSummary>, includeLeaf: Boolean = true): List<ResidentialLocation> {
    if (!hasChildren(locations)) return emptyList()

    return locations.filter { it.parentLocationId == locationId }
      .filter { includeLeaf || it.hasChildren(allLocations = locations) }.map {
        it.toDto(locations, includeLeaf)
      }
  }

  fun isNotACellAndCertified() = isCertified() && locationType in listOf("WING", "BLK", "LAND", "SPUR", "TIER")

  fun isCellOrRoom() = locationType in listOf("ROOM", "CELL")

  fun isCertified() = certified == "Y"

  fun hasParent() = parentLocationId != null

  fun getPathHierarchy(): String = LocationProcessor.stripAgencyId(fullLocationPath, prisonId)

  fun getLocalNameDescription(): String? = if (locationType == "CELL") {
    locationCode
  } else {
    LocationProcessor.formatLocation(localName ?: locationCode)
  }

  fun hasChildren(allLocations: List<PrisonRollCountSummary>): Boolean = allLocations.firstOrNull { it.parentLocationId == locationId } != null
}
