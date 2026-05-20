package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.api.model.CaseLoad
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.repository.AgencyRepository
import uk.gov.justice.hmpps.prison.repository.LocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import java.util.Optional

/**
 * Test cases for [LocationService].
 */
internal class LocationServiceImplTest {
  private val locationRepository: LocationRepository = mock()
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository = mock()
  private val agencyRepository: AgencyRepository = mock()
  private val caseLoadService: CaseLoadService = mock()

  private var locationService = LocationService(agencyRepository, agencyInternalLocationRepository, locationRepository, null, caseLoadService)

  @Test
  fun getUserLocations() {
    val agencies = listOf(Agency.builder().agencyId("LEI").build())

    whenever(agencyRepository.findAgenciesForCurrentCaseloadByUsername("me")).thenReturn(agencies)

    val location: Location = createTestLocation()
    whenever(
      agencyInternalLocationRepository.findByAgencyIdAndActiveAndParentLocationIsNullAndCapacityGreaterThanAndTypeIsNotNull(
        "LEI",
        true,
        0,
      ),
    ).thenReturn(
      listOf(createTestAgencyInternalLocation()),
    )
    whenever(caseLoadService.getWorkingCaseLoadForUser("me"))
      .thenReturn(Optional.of(CaseLoad.builder().caseLoadId("LEI").type("INST").build()))
    val returnedLocations = locationService.getUserLocations("me", false)

    assertThat(returnedLocations.isEmpty()).isFalse()
    assertThat(returnedLocations).hasSize(2)

    val returnedLocation = returnedLocations[1]
    assertThat(returnedLocation.locationId).isEqualTo(location.locationId)
    assertThat(returnedLocation.agencyId).isEqualTo(location.agencyId)
    assertThat(returnedLocation.locationType).isEqualTo(location.locationType)
    assertThat(returnedLocation.description).isEqualTo(location.description)
  }

  @Test
  fun getUserLocationsWithCentralOnly() {
    whenever(caseLoadService.getWorkingCaseLoadForUser("admin"))
      .thenReturn(Optional.of(CaseLoad.builder().caseLoadId("CADM_I").type("ADMIN").build()))
    val returnedLocations = locationService.getUserLocations("admin", false)

    assertThat(returnedLocations).isEmpty()
  }

  @Test
  fun getUserLocationsWithNoCaseload() {
    whenever(caseLoadService.getWorkingCaseLoadForUser("noone"))
      .thenReturn(Optional.empty())
    val returnedLocations = locationService.getUserLocations("noone", false)

    assertThat(returnedLocations).isEmpty()
  }

  companion object {
    private fun createTestLocation(): Location {
      val location = Location()

      location.locationId = 1L
      location.agencyId = "LEI"
      location.locationType = "WING"
      location.description = "A"

      return location
    }

    private fun createTestAgencyInternalLocation(): AgencyInternalLocation? = AgencyInternalLocation.builder()
      .locationId(1L)
      .agencyId("LEI")
      .locationType("WING")
      .description("LEI-A")
      .certifiedFlag(true)
      .build()
  }
}
