package uk.gov.justice.hmpps.prison.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess

@Service
@Transactional(readOnly = true)
class OffenderLocationService(
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository,
  private val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository,
) {
  @VerifyBookingAccess(overrideRoles = ["SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"])
  fun getOffenderLocation(@Suppress("UNUSED_PARAMETER") bookingId: Long, summary: OffenderSummary): OffenderLocation {
    // booking not active or no current internal location then no location to report
    if (summary.currentlyInPrison != "Y" || summary.internalLocationId == null) return OffenderLocation()

    val currentLocationId = summary.internalLocationId.toLong()
    val location = agencyInternalLocationRepository.findOneByLocationId(currentLocationId)
      .orElseThrow { RuntimeException() }

    val lastPermanentLocation = if (isTemporaryLocation(location.locationCode)) {
      val cells = bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(
        bookingId,
        PageRequest.of(0, 10, Sort.by("bedAssignmentHistoryPK.sequence").descending()),
      )
      // switch to sequence so that we don't get the location (lazily) for each bed history if we don't need it
      cells.asSequence()
        // must be at same prison without going to different prison in meantime
        .takeWhile { it.location.agencyId == summary.agencyLocationId }
        .firstOrNull { !isTemporaryLocation(it.location?.locationCode) }
        ?.location
    } else {
      null
    }
    return OffenderLocation(
      levels = location.getLocationWithParents(),
      lastPermanentLevels = lastPermanentLocation?.getLocationWithParents(),
    )
  }

  private fun isTemporaryLocation(locationCode: String?): Boolean =
    setOf("RECP", "RECEP", "RECEPTION", "COURT", "TAP", "ECL", "CSWAP").contains(locationCode)
}

private fun AgencyInternalLocation.getLocationWithParents(): List<HousingLocation> =
  this.parentLocation?.getLocationWithParents()?.let {
    it.plus(HousingLocation(it.size + 1, this.locationCode, this.livingUnit, this.userDescription))
  } ?: listOf(HousingLocation(1, this.locationCode, this.livingUnit, this.userDescription))

@JsonInclude(NON_NULL)
data class OffenderLocation(
  @Schema(description = "Current housing levels or null if not currently in prison")
  val levels: List<HousingLocation>? = null,
  @Schema(description = "Previous permanent housing levels at the same prison without moving to a different prison inbetween")
  val lastPermanentLevels: List<HousingLocation>? = null,
)

@JsonInclude(NON_NULL)
data class HousingLocation(
  @Schema(
    description = "The level (starting from 1) of the individual location. The highest number level will be the cell.",
    minimum = "1",
    maximum = "4",
    example = "1",
  )
  val level: Int,
  @Schema(description = "The code for the location e.g. 010 for a cell, A for a wing", example = "010")
  val code: String,
  @JsonIgnore
  private val livingUnit: LivingUnitReferenceCode?,
  @JsonIgnore
  private val userDescription: String?,
) {
  val type: String?
    @Schema(
      description = "The type of the location - from LIVING_UNIT reference code",
      example = "WING",
      allowableValues = ["BED", "BLK", "CB", "CELL", "LAND", "SPUR", "TIER", "WING"],
    )
    get() = livingUnit?.code

  val description: String
    @Schema(
      description = "Description of the location, either from the user description if set or reference code description and code",
      example = "Wing A",
    )
    get() = userDescription ?: livingUnit?.description?.let { "$it $code" } ?: code
}
