package uk.gov.justice.hmpps.prison.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.MovementsRepository
import uk.gov.justice.hmpps.prison.repository.PrisonRollCountSummaryRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class PrisonRollCountService(
  private val prisonRollCountSummaryRepository: PrisonRollCountSummaryRepository,
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository,
  private val movementsRepository: MovementsRepository,

) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
  fun getPrisonRollCount(prisonId: String, includeCells: Boolean, locationId: String? = null): PrisonRollCount {
    val rollCount = prisonRollCountSummaryRepository.findAllByPrisonId(prisonId).sortedBy { it.fullLocationPath }

    val residentialLocationList = rollCount.filter { it.isNotACellAndCertified() || it.isCellOrRoom() }

    val residentialLocations = residentialLocationList.filter { !it.hasParent() }
      .map { it.toDto(locations = residentialLocationList, includeLeaf = includeCells) }

    val certifiedTopLevelLocations = rollCount.filter { !it.hasParent() && it.isCertified() }
    val nonCertifiedTopLevelLocations = rollCount.filter { !it.hasParent() && !it.isCertified() }

    val unassignedIn = nonCertifiedTopLevelLocations.sumOf { it.currentlyInCell?:0 } + nonCertifiedTopLevelLocations.sumOf { it.outOfLivingUnits?:0}
    val currentRoll = certifiedTopLevelLocations.sumOf { it.currentlyInCell?:0 } + certifiedTopLevelLocations.sumOf { it.outOfLivingUnits?:0} + unassignedIn

    val now = LocalDate.now()
    val enRouteCount = movementsRepository.getEnrouteMovementsOffenderCount(prisonId, now)
    val movementCount = movementsRepository.getMovementCount(prisonId, now)
    val cSwap = agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive(prisonId, "CSWAP", true).first()

    return PrisonRollCount(
      prisonId = prisonId,
      numUnlockRollToday = currentRoll - movementCount.getIn() + movementCount.getOut(),
      numCurrentPopulation = currentRoll,
      numOutToday = movementCount.getOut(),
      numInReception = unassignedIn,
      numArrivedToday = movementCount.getIn(),
      numStillToArrive = enRouteCount,
      numNoCellAllocated = cSwap?.currentOccupancy ?:0,
      totals = LocationRollCount(
        bedsInUse = residentialLocations.sumOf { it.rollCount.bedsInUse },
        currentlyInCell = residentialLocations.sumOf { it.rollCount.currentlyInCell },
        currentlyOut = residentialLocations.sumOf { it.rollCount.currentlyOut },
        workingCapacity = residentialLocations.sumOf { it.rollCount.workingCapacity },
        netVacancies = residentialLocations.sumOf { it.rollCount.netVacancies },
        outOfOrder = residentialLocations.sumOf { it.rollCount.outOfOrder },
      ),
      locations = residentialLocations
    )
  }

  fun getPrisonCellRollCount(prisonId: String, locationId: String): PrisonRollCount {
    val rollCount = getPrisonRollCount(prisonId = prisonId, includeCells = true)
    return rollCount.copy(locations = rollCount.findSubLocations(locationId))
  }
}

@Schema(description = "Establishment Roll Count")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PrisonRollCount(
  @Schema(description = "Prison Id", required = true)
  val prisonId: String,
  val numUnlockRollToday: Int,
  val numCurrentPopulation: Int,
  val numArrivedToday: Int,
  val numInReception: Int,
  val numStillToArrive: Int,
  val numOutToday: Int,
  val numNoCellAllocated: Int,

  val totals: LocationRollCount,

  @Schema(description = "Locations", required = true)
  val locations: List<ResidentialLocation>,
 ) {

  fun findSubLocations(parentLocationId: String): List<ResidentialLocation> {
    val subLocations = mutableListOf<ResidentialLocation>()

    fun traverse(locations: List<ResidentialLocation>, parentLocationId: String) {

        for (childLocation in locations) {
          if (childLocation.locationId == parentLocationId) {
            subLocations.add(childLocation)
          }
          traverse(childLocation.subLocations, parentLocationId)
        }
    }

    traverse(locations, parentLocationId)
    return subLocations
  }
}

@Schema(description = "Residential Roll Count Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResidentialLocation(
  @Schema(description = "Prison Id", required = true)
  val locationId: String,
  val locationType: String,
  val locationCode: String,
  val fullLocationPath: String,
  val certified: Boolean,
  val localName: String? = null,
  val rollCount: LocationRollCount,
  val subLocations: List<ResidentialLocation>,
)

data class LocationRollCount(
  val bedsInUse: Int = 0,
  val currentlyInCell: Int = 0,
  val currentlyOut: Int = 0,
  val workingCapacity: Int = 0,
  val netVacancies: Int = 0,
  val outOfOrder: Int = 0,
)
