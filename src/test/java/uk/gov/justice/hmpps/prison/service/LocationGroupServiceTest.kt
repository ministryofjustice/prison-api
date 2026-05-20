package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.api.model.LocationGroup
import uk.gov.justice.hmpps.prison.repository.LocationRepository
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor
import java.util.stream.Collectors

internal class LocationGroupServiceTest {
  private companion object {
    private val L1: Location =
      Location.builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("BLOCK A")
        .internalLocationCode("A").build()
    private val L2: Location =
      Location.builder().locationId(-13L).locationType("WING").description("LEI-H").internalLocationCode("H").build()

    private val SL1: Location =
      Location.builder().locationId(-14L).locationType("LAND").description("LEI-H-1").parentLocationId(-13L)
        .userDescription("LANDING H/1").internalLocationCode("1").build()
    private val SL2: Location =
      Location.builder().locationId(-2L).locationType("LAND").description("LEI-A-1").parentLocationId(-1L)
        .userDescription("LANDING A/1").internalLocationCode("1").build()
    private val SL3: Location =
      Location.builder().locationId(-32L).locationType("LAND").description("LEI-A-2").parentLocationId(-1L)
        .userDescription("LANDING A/2").internalLocationCode("2").build()

    private val CELL_A_1: Location =
      Location.builder().locationId(-320L).locationType("CELL").description("LEI-A-1-001").parentLocationId(-32L)
        .build()
    private val CELL_AA_1: Location =
      Location.builder().locationId(-320L).locationType("CELL").description("LEI-AA-1-001").parentLocationId(-32L)
        .build()
    private val CELL_A_3: Location =
      Location.builder().locationId(-320L).locationType("CELL").description("LEI-A-3-001").parentLocationId(-32L)
        .build()
    private val CELL_B_1: Location =
      Location.builder().locationId(-320L).locationType("CELL").description("LEI-B-2-001").parentLocationId(-32L)
        .build()

    private val LG1: LocationGroup = LocationGroup.builder().key("A").name("Block A").build()
    private val LG2: LocationGroup = LocationGroup.builder().key("H").name("H").build()

    private val SLG2: LocationGroup = LocationGroup.builder().key("1").name("Landing A/1").build()
    private val SLG3: LocationGroup = LocationGroup.builder().key("2").name("Landing A/2").build()

    private val L1_WITH_ACRONYM: Location =
      Location.builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("Mpu")
        .internalLocationCode("A").build()
    private val SL1_WITH_ACRONYM: Location =
      Location.builder().locationId(-14L).locationType("LAND").description("LEI-H-1").parentLocationId(-1L)
        .userDescription("LANDING H/1 Dru").internalLocationCode("1").build()
    private val SL2_WITH_ACRONYM: Location =
      Location.builder().locationId(-2L).locationType("LAND").description("LEI-A-1").parentLocationId(-1L)
        .userDescription("dart LANDING A/1").internalLocationCode("2").build()

    private val LG1_WITH_ACRONYM: LocationGroup = LocationGroup.builder().key("A").name("MPU").build()
    private val SLG1_WITH_ACRONYM: LocationGroup = LocationGroup.builder().key("1").name("Landing H/1 DRU").build()
    private val SLG2_WITH_ACRONYM: LocationGroup = LocationGroup.builder().key("2").name("DART Landing A/1").build()

    private fun locationStream(vararg locations: Location): List<Location> = locations.map { LocationProcessor.processLocation(it) }
  }

  private val repository: LocationRepository = mock()
  private val service = LocationGroupService(repository)

  @Test
  fun noGroups() {
    assertThat(service.getLocationGroups("LEI")).isEmpty()
  }

  @Test
  fun oneGroup() {
    whenever(repository.getLocationGroupData("LEI")).thenReturn(listOf(L1))
    assertThat(service.getLocationGroups("LEI")).contains(LG1)
  }

  @Test
  fun twoGroups() {
    whenever(repository.getLocationGroupData("LEI"))
      .thenReturn(listOf(L2, L1))
    assertThat(service.getLocationGroups("LEI")).containsExactly(LG1, LG2)
  }

  @Test
  fun oneGroupOneSubGroupRemoved() {
    whenever(repository.getLocationGroupData("LEI")).thenReturn(listOf(L1))
    whenever(repository.getSubLocationGroupData(setOf(-1L))).thenReturn(
      listOf(SL2),
    )
    assertThat(service.getLocationGroups("LEI")).contains(LG1)
  }

  @Test
  fun oneGroupTwoSubGroups() {
    whenever(repository.getLocationGroupData("LEI")).thenReturn(listOf(L1))
    whenever(repository.getSubLocationGroupData(setOf(-1L))).thenReturn(
      listOf(SL3, SL2),
    )
    assertThat(service.getLocationGroups("LEI")).contains(
      LG1.toBuilder().children(listOf(SLG2, SLG3)).build(),
    )
  }

  @Test
  fun twoGroupWithSubGroups() {
    whenever(repository.getLocationGroupData("LEI"))
      .thenReturn(listOf(L1, L2))
    whenever(repository.getSubLocationGroupData(setOf(-1L, -13L))).thenReturn(
      listOf(SL1, SL2, SL3),
    )
    assertThat(service.getLocationGroups("LEI")).contains(
      LG1.toBuilder().children(listOf(SLG2, SLG3)).build(),
      LG2,
    )
  }

  @Test
  fun locationGroupFilters() {
    val filter = service.locationGroupFilter("LEI", "A")
    assertThat(locationStream(CELL_A_1, CELL_A_3, CELL_B_1, CELL_AA_1).stream().filter(filter).collect(Collectors.toList()))
      .containsExactlyInAnyOrderElementsOf(locationStream(CELL_A_1, CELL_A_3))
  }

  @Test
  fun locationGroupFormatting() {
    whenever(repository.getLocationGroupData("LEI"))
      .thenReturn(listOf(L1_WITH_ACRONYM))
    whenever(repository.getSubLocationGroupData(setOf(-1L))).thenReturn(
      listOf(SL1_WITH_ACRONYM, SL2_WITH_ACRONYM),
    )
    val foundLocations = service.getLocationGroups("LEI")
    assertThat(foundLocations).contains(
      LG1_WITH_ACRONYM.toBuilder().children(listOf(SLG2_WITH_ACRONYM, SLG1_WITH_ACRONYM)).build(),
    )
  }
}
