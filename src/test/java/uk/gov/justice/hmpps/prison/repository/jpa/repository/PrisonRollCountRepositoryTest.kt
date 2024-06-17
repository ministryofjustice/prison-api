package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.PrisonRollCountSummaryRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
class PrisonRollCountRepositoryTest {

  @Autowired
  private lateinit var repository: PrisonRollCountSummaryRepository

  @Nested
  inner class FindRollSummaryForPrison {
    @Test
    fun findRollSummaryForPrison() {
      val rollCountSummary = repository.findAllByPrisonId("LEI")
      assertThat(rollCountSummary).isNotEmpty
      assertThat(rollCountSummary).hasSize(28)
      assertThat(rollCountSummary)
        .extracting(
          "locationId",
          "locationType",
          "fullLocationPath",
          "locationCode",
          "localName",
          "parentLocationId",
          "bedsInUse",
          "currentlyInCell",
          "outOfLivingUnits",
          "currentlyOut",
          "operationalCapacity",
          "netVacancies",
          "maximumCapacity",
          "availablePhysical",
          "outOfOrder",
        )
        .contains(
          Assertions.tuple(-1L, "WING", "LEI-A", "A", "BLOCK A", null, 12, 11, 1, 0, 13, 1, 14, 2, 3),
          Assertions.tuple(-2L, "LAND", "LEI-A-1", "1", "LANDING A/1", -1L, 12, 11, 1, 0, 13, 1, 14, 2, 1),
          Assertions.tuple(-13L, "WING", "LEI-H", "H", null, null, 14, 12, 0, 2, 20, 6, 20, 6, 0),
          Assertions.tuple(-14L, "LAND", "LEI-H-1", "1", "LANDING H/1", -13L, 14, 12, 0, 2, 20, 6, 20, 6, 0),
        )
    }

    @Test
    fun findRollSummaryForPrisonWingAndLandings() {
      val rollCountSummary = repository.findAllByPrisonIdAndLocationTypeInAndCertified("LEI", listOf("WING", "LAND"), "Y")
      assertThat(rollCountSummary).isNotEmpty
      assertThat(rollCountSummary).hasSize(4)
      assertThat(rollCountSummary)
        .extracting(
          "locationId",
          "locationType",
          "fullLocationPath",
          "locationCode",
          "localName",
          "parentLocationId",
          "bedsInUse",
          "currentlyInCell",
          "outOfLivingUnits",
          "currentlyOut",
          "operationalCapacity",
          "netVacancies",
          "maximumCapacity",
          "availablePhysical",
          "outOfOrder",
        )
        .contains(
          Assertions.tuple(-1L, "WING", "LEI-A", "A", "BLOCK A", null, 12, 11, 1, 0, 13, 1, 14, 2, 3),
          Assertions.tuple(-2L, "LAND", "LEI-A-1", "1", "LANDING A/1", -1L, 12, 11, 1, 0, 13, 1, 14, 2, 1),
          Assertions.tuple(-13L, "WING", "LEI-H", "H", null, null, 14, 12, 0, 2, 20, 6, 20, 6, 0),
          Assertions.tuple(-14L, "LAND", "LEI-H-1", "1", "LANDING H/1", -13L, 14, 12, 0, 2, 20, 6, 20, 6, 0),
        )
    }

    @Test
    fun findRollSummaryForPrisonWingAndLandingAndCells() {
      val rollCountSummary = repository.findAllByPrisonIdAndLocationTypeInAndCertified("LEI", listOf("WING", "LAND", "CELL"), "Y")
      assertThat(rollCountSummary).isNotEmpty
      assertThat(rollCountSummary).hasSize(27)
      assertThat(rollCountSummary)
        .extracting(
          "locationId",
          "locationType",
          "fullLocationPath",
          "locationCode",
          "localName",
          "parentLocationId",
          "bedsInUse",
          "currentlyInCell",
          "outOfLivingUnits",
          "currentlyOut",
          "operationalCapacity",
          "netVacancies",
          "maximumCapacity",
          "availablePhysical",
          "outOfOrder",
        )
        .contains(
          Assertions.tuple(-1L, "WING", "LEI-A", "A", "BLOCK A", null, 12, 11, 1, 0, 13, 1, 14, 2, 3),
          Assertions.tuple(-2L, "LAND", "LEI-A-1", "1", "LANDING A/1", -1L, 12, 11, 1, 0, 13, 1, 14, 2, 1),
          Assertions.tuple(-13L, "WING", "LEI-H", "H", null, null, 14, 12, 0, 2, 20, 6, 20, 6, 0),
          Assertions.tuple(-14L, "LAND", "LEI-H-1", "1", "LANDING H/1", -13L, 14, 12, 0, 2, 20, 6, 20, 6, 0),
        )
    }

    @Test
    fun findRollSummaryNotCertified() {
      val rollCountSummary = repository.findAllByPrisonIdAndCertified("LEI", "N")
      assertThat(rollCountSummary).isNotEmpty
      assertThat(rollCountSummary).hasSize(1)
      assertThat(rollCountSummary)
        .extracting(
          "locationId",
          "locationType",
          "fullLocationPath",
          "locationCode",
          "localName",
        )
        .contains(
          Assertions.tuple(-25L, "AREA", "LEI-CHAP", "CHAP", "Chapel"),
        )
    }
  }
}
