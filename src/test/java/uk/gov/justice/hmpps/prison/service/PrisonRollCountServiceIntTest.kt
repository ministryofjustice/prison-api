package uk.gov.justice.hmpps.prison.service

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class PrisonRollCountServiceIntTest {

  private val prisonId = "LEI"

  @Autowired
  lateinit var service: PrisonRollCountService

  @Nested
  inner class FindRollSummaryForPrison {
    @Test
    @Disabled("As of 7 may 2025 this test is sometimes failing with various counts out by 1. Ideally it needs to use locally set up data.")
    fun findRollSummaryForPrisonWithoutCells() {
      val result = service.getPrisonRollCount(prisonId, includeCells = false)

      assertThat(result.prisonId).isEqualTo(prisonId)
      assertThat(result.locations).hasSize(2)
      val subLocations = result.locations[0].subLocations
      assertThat(subLocations).hasSize(1)
      assertThat(subLocations[0].subLocations).isEmpty()

      assertThat(result.numUnlockRollToday).isEqualTo(25)
      assertThat(result.numCurrentPopulation).isEqualTo(23)
      assertThat(result.numArrivedToday).isEqualTo(0)
      assertThat(result.numInReception).isEqualTo(0)
      assertThat(result.numStillToArrive).isEqualTo(2)
      assertThat(result.numOutToday).isGreaterThanOrEqualTo(2)
      assertThat(result.numNoCellAllocated).isEqualTo(0)

      assertThat(result.totals.bedsInUse).isEqualTo(25)
      assertThat(result.totals.currentlyInCell).isEqualTo(22)
      assertThat(result.totals.currentlyOut).isEqualTo(2)
      assertThat(result.totals.workingCapacity).isEqualTo(33)
      assertThat(result.totals.netVacancies).isEqualTo(8)
      assertThat(result.totals.outOfOrder).isEqualTo(3)
    }

    @Test
    @Disabled("As of 7 may 2025 this test is sometimes failing with various counts out by 1. Ideally it needs to use locally set up data.")
    fun findRollSummaryForPrisonWithCells() {
      val result = service.getPrisonRollCount(prisonId, includeCells = true)

      assertThat(result.prisonId).isEqualTo(prisonId)
      assertThat(result.locations).hasSize(2)
      val subLocations = result.locations[0].subLocations
      assertThat(subLocations).hasSize(1)
      assertThat(subLocations[0].subLocations).hasSize(13)

      assertThat(result.numUnlockRollToday).isEqualTo(25)
      assertThat(result.numCurrentPopulation).isEqualTo(23)
      assertThat(result.numArrivedToday).isEqualTo(0)
      assertThat(result.numInReception).isEqualTo(0)
      assertThat(result.numStillToArrive).isEqualTo(2)
      assertThat(result.numOutToday).isGreaterThanOrEqualTo(2)
      assertThat(result.numNoCellAllocated).isEqualTo(0)

      assertThat(result.totals.bedsInUse).isEqualTo(25)
      assertThat(result.totals.currentlyInCell).isEqualTo(22)
      assertThat(result.totals.currentlyOut).isEqualTo(2)
      assertThat(result.totals.workingCapacity).isEqualTo(33)
      assertThat(result.totals.netVacancies).isEqualTo(8)
      assertThat(result.totals.outOfOrder).isEqualTo(3)
    }
  }
}
