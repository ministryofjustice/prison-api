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
import uk.gov.justice.hmpps.prison.util.NaturalOrderComparator
import uk.gov.justice.hmpps.prison.util.SortAttribute
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
      .sortedWith (NaturalOrderComparator())

    val certifiedTopLevelLocations = rollCount.filter { !it.hasParent() && it.isCertified() }
    val nonCertifiedTopLevelLocations = rollCount.filter { !it.hasParent() && !it.isCertified() }

    val unassignedIn = nonCertifiedTopLevelLocations.sumOf { it.currentlyInCell ?: 0 } + nonCertifiedTopLevelLocations.sumOf { it.outOfLivingUnits ?: 0 }
    val currentRoll = certifiedTopLevelLocations.sumOf { it.currentlyInCell ?: 0 } + certifiedTopLevelLocations.sumOf { it.outOfLivingUnits ?: 0 } + unassignedIn

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
      numNoCellAllocated = cSwap?.currentOccupancy ?: 0,
      totals = LocationRollCount(
        bedsInUse = residentialLocations.sumOf { it.rollCount.bedsInUse },
        currentlyInCell = residentialLocations.sumOf { it.rollCount.currentlyInCell },
        currentlyOut = residentialLocations.sumOf { it.rollCount.currentlyOut },
        workingCapacity = residentialLocations.sumOf { it.rollCount.workingCapacity },
        netVacancies = residentialLocations.sumOf { it.rollCount.netVacancies },
        outOfOrder = residentialLocations.sumOf { it.rollCount.outOfOrder },
      ),
      locations = residentialLocations,
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
  @Schema(description = "Unlock roll today", required = true)
  val numUnlockRollToday: Int,
  @Schema(description = "Current population", required = true)
  val numCurrentPopulation: Int,
  @Schema(description = "Arrived today", required = true)
  val numArrivedToday: Int,
  @Schema(description = "In reception", required = true)
  val numInReception: Int,
  @Schema(description = "Still to arrive", required = true)
  val numStillToArrive: Int,
  @Schema(description = "Out today", required = true)
  val numOutToday: Int,
  @Schema(description = "No cell allocated", required = true)
  val numNoCellAllocated: Int,

  @Schema(description = "Totals", required = true)
  val totals: LocationRollCount,

  @Schema(description = "Residential location roll count summary", required = true)
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
  @Schema(description = "Location Id", required = true, example = "121212")
  val locationId: String,
  @Schema(description = "Type of location", required = true, example = "CELL")
  val locationType: String,
  @Schema(description = "Code of this location", required = true, example = "002")
  val locationCode: String,
  @Schema(description = "Path of this location from top level", required = true, example = "A-1-002")
  val fullLocationPath: String,
  @Schema(description = "Certified location", required = true, example = "true")
  val certified: Boolean,
  @Schema(description = "Local name of the location", required = false, example = "Wing A")
  val localName: String? = null,
  @Schema(description = "Summary of cell roll count for this level (aggregated)", required = true)
  val rollCount: LocationRollCount,
  @Schema(description = "List of residential locations for this summary, including wings and sub-locations such as landings and cells", required = true)
  val subLocations: List<ResidentialLocation>,
) : SortAttribute {

  override val key: String
    get() = localName?.capitalizeWords() ?: fullLocationPath
}

@Schema(description = "Summary of cell usage for this level")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocationRollCount(
  @Schema(description = "Beds in use", required = true)
  val bedsInUse: Int = 0,
  @Schema(description = "Currently in cell", required = true)
  val currentlyInCell: Int = 0,
  @Schema(description = "Currently out", required = true)
  val currentlyOut: Int = 0,
  @Schema(description = "Working capacity", required = true)
  val workingCapacity: Int = 0,
  @Schema(description = "Net vacancies", required = true)
  val netVacancies: Int = 0,
  @Schema(description = "Out of order", required = true)
  val outOfOrder: Int = 0,
)

fun String.capitalizeWords(delimiter: String = " ") =
  split(delimiter).joinToString(delimiter) { word ->

    val smallCaseWord = word.lowercase()
    smallCaseWord.replaceFirstChar(Char::titlecaseChar)
  }