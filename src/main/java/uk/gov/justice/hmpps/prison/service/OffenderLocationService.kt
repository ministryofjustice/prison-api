package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess

@Service
@Transactional(readOnly = true)
class OffenderLocationService(
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository,
) {
  @VerifyBookingAccess(overrideRoles = ["SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"])
  fun getOffenderLocation(@Suppress("UNUSED_PARAMETER") bookingId: Long, summary: OffenderSummary): OffenderLocation {
    // booking not active or no current internal location then no location to report
    if (summary.currentlyInPrison != "Y" || summary.internalLocationId == null) return OffenderLocation()

    val location = agencyInternalLocationRepository.findOneByLocationId(summary.internalLocationId.toLong())
      .orElseThrow { RuntimeException() }

    return OffenderLocation(levels = location.getLocationWithParents())
  }
}

private fun AgencyInternalLocation.getLocationWithParents(): List<HousingLocation> =
  this.parentLocation?.getLocationWithParents()?.let {
    it.plus(HousingLocation(it.size + 1, this.locationCode, this.livingUnit))
  } ?: listOf(HousingLocation(1, this.locationCode, this.livingUnit))

data class OffenderLocation(val levels: List<HousingLocation>? = null)

data class HousingLocation(
  val level: Int,
  val code: String,
  private val livingUnit: LivingUnitReferenceCode?
) {
  val type: String?
    get() = livingUnit?.code
}
