package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgencyInternalLocationTest {
  @Test
  fun testCellCswap_isFalse() {
    val location = AgencyInternalLocation.builder().build()

    assertThat(location.isCellSwap).isEqualTo(false)
  }

  @Test
  fun testCellCswap_isTrue() {
    val location = AgencyInternalLocation
      .builder()
      .active(true)
      .certifiedFlag(false)
      .parentLocation(null)
      .locationCode("CSWAP")
      .build()

    assertThat(location.isCellSwap).isEqualTo(true)
  }

  @Test
  fun testHasSpace_NotActiveNotCell() {
    val location = AgencyInternalLocation.builder()
      .capacity(100)
      .currentOccupancy(50)
      .build()

    assertThat(location.isActiveCellWithSpace).isEqualTo(false)
  }

  @Test
  fun testHasSpace_IgnoreZeroOperationalCapacity() {
    val location = AgencyInternalLocation.builder()
      .active(true)
      .locationType("CELL")
      .operationalCapacity(0)
      .capacity(10)
      .currentOccupancy(5)
      .build()

    assertThat(location.isActiveCellWithSpace).isEqualTo(true)
  }

  @Test
  fun testHasSpace_NotFull() {
    val location = AgencyInternalLocation.builder()
      .active(true)
      .locationType("CELL")
      .capacity(100)
      .currentOccupancy(50)
      .build()

    assertThat(location.isActiveCellWithSpace).isEqualTo(true)
  }

  @Test
  fun testHasSpace_Full() {
    val location = AgencyInternalLocation.builder()
      .active(true)
      .locationType("CELL")
      .capacity(100)
      .currentOccupancy(100)
      .build()

    assertThat(location.isActiveCellWithSpace).isEqualTo(false)
  }

  @Test
  fun testCapacity_IgnoreZeroOperationalCapacity() {
    val location = AgencyInternalLocation.builder()
      .active(true)
      .locationType("CELL")
      .operationalCapacity(0)
      .capacity(10)
      .currentOccupancy(5)
      .build()

    assertThat(location.getActualCapacity()).isEqualTo(10)
  }
}
