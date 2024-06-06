package uk.gov.justice.hmpps.prison.service

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class PrisonRollCountServiceIntTest {

  @Autowired
  lateinit var service: PrisonRollCountService

  @Nested
  inner class FindRollSummaryForPrison {
    @Test
    fun findRollSummaryForPrisonWithoutCells() {
      val prisonId = "LEI"
      val includeCells = false

      val result = service.getPrisonRollCount(prisonId, includeCells)

      assertThat(result.prisonId).isEqualTo(prisonId)
      assertThat(result.locations).hasSize(2)
      val subLocations = result.locations[0].subLocations
      assertThat(subLocations).hasSize(1)
      assertThat(subLocations[0].subLocations).isEmpty()

      assertThat(result.numUnlockRollToday).isGreaterThanOrEqualTo(24)
      assertThat(result.numCurrentPopulation).isGreaterThanOrEqualTo(24)
      assertThat(result.numArrivedToday).isEqualTo(0)
      assertThat(result.numInReception).isEqualTo(0)
      assertThat(result.numStillToArrive).isEqualTo(2)
      assertThat(result.numOutToday).isEqualTo(0)
      assertThat(result.numNoCellAllocated).isEqualTo(0)

      assertThat(result.totals.bedsInUse).isEqualTo(26)
      assertThat(result.totals.currentlyInCell).isEqualTo(23)
      assertThat(result.totals.currentlyOut).isEqualTo(2)
      assertThat(result.totals.workingCapacity).isEqualTo(33)
      assertThat(result.totals.netVacancies).isEqualTo(7)
      assertThat(result.totals.outOfOrder).isEqualTo(3)
    }

    @Test
    fun findRollSummaryForPrisonWithCells() {
      val prisonId = "LEI"
      val includeCells = true

      val result = service.getPrisonRollCount(prisonId, includeCells)

      assertThat(result.prisonId).isEqualTo(prisonId)
      assertThat(result.locations).hasSize(2)
      val subLocations = result.locations[0].subLocations
      assertThat(subLocations).hasSize(1)
      assertThat(subLocations[0].subLocations).hasSize(13)

      assertThat(result.numUnlockRollToday).isGreaterThanOrEqualTo(24)
      assertThat(result.numCurrentPopulation).isGreaterThanOrEqualTo(24)
      assertThat(result.numArrivedToday).isEqualTo(0)
      assertThat(result.numInReception).isEqualTo(0)
      assertThat(result.numStillToArrive).isEqualTo(2)
      assertThat(result.numOutToday).isEqualTo(0)
      assertThat(result.numNoCellAllocated).isEqualTo(0)

      assertThat(result.totals.bedsInUse).isEqualTo(26)
      assertThat(result.totals.currentlyInCell).isEqualTo(23)
      assertThat(result.totals.currentlyOut).isEqualTo(2)
      assertThat(result.totals.workingCapacity).isEqualTo(33)
      assertThat(result.totals.netVacancies).isEqualTo(7)
      assertThat(result.totals.outOfOrder).isEqualTo(3)
    }
  }
}
