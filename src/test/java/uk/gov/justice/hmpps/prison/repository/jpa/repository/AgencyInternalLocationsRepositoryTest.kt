package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitReferenceCode

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AgencyInternalLocationsRepositoryTest {
  @Autowired
  private lateinit var repository: AgencyInternalLocationRepository

  @Test
  fun findLocationsByAgencyIdAndLocationTypeAndActive_returnsAllLocations() {
    val locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("SYI", "CELL", true)
    assertThat(locations).extracting("locationId").containsExactlyInAnyOrder(-202L, -204L, -207L)
  }

  @Test
  fun findLocationsByAgencyIdAndLocationTypeAndActive_hydratesReturnObject() {
    val parentParentLocation = AgencyInternalLocation.builder().locationId(-205L).locationType("WING").agencyId("SYI")
      .currentOccupancy(20).operationalCapacity(20).description("SYI-H").capacity(null)
      .certifiedFlag(true).locationCode("H").active(true).build()
    val parentLocation =
      AgencyInternalLocation.builder().locationId(-206L).locationType("LAND").agencyId("SYI").capacity(null)
        .currentOccupancy(20).operationalCapacity(20).description("SYI-H-1").parentLocation(parentParentLocation)
        .userDescription("Landing H/1")
        .certifiedFlag(true).locationCode("1").active(true).build()
    val expected = AgencyInternalLocation.builder().locationId(-207L).locationType("CELL").agencyId("SYI")
      .currentOccupancy(1).operationalCapacity(1).description("SYI-H-1-1").parentLocation(parentLocation)
      .userDescription("Cell H/1-1")
      .certifiedFlag(true).locationCode("1").active(true).build()
    val locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("SYI", "CELL", true)
    val actual = locations.first { it.locationId == expected.locationId }
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun findCellSwapLocation() {
    val location = repository.findByLocationCodeAndAgencyId("CSWAP", "LEI")
      .first()
    assertThat(location.description).isEqualTo("LEI-CSWAP")
  }

  @Test
  fun findLivingUnit() {
    val location = repository.findOneByLocationId(-200L).orElseThrow()
    assertThat(location.locationType).isEqualTo("WING")
    assertThat(location.livingUnit).isEqualTo(LivingUnitReferenceCode("WING", "Wing"))
  }
}
