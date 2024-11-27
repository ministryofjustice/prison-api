package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.Area
import uk.gov.justice.hmpps.prison.repository.jpa.model.Region
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import kotlin.jvm.optionals.getOrNull

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
@WithMockAuthUser
class AreaRepositoryTest {
  @Autowired
  private lateinit var repository: AgencyAreaRepository

  @Autowired
  private lateinit var areaRepository: AreaRepository

  @Autowired
  private lateinit var regionRepository: RegionRepository

  @Test
  fun findAllAreasForAllTypes() {
    val areas = repository.findAll()
    assertThat(areas).isNotEmpty
  }

  @Test
  fun findAllAreas() {
    val areas = areaRepository.findAll()
    assertThat(areas).isNotEmpty
    assertThat(areas.count { it.code == "LONDON" }).isEqualTo(1)
  }

  @Test
  fun findAllRegions() {
    val regions = regionRepository.findAll()
    assertThat(regions).isNotEmpty
    assertThat(regions.count { it.code == "LON" }).isEqualTo(1)
  }

  @Test
  fun createRegion() {
    val region = Region(code = "TEST", description = "TEST")
    regionRepository.saveAndFlush(region)

    val retrievedRegion = regionRepository.findById("TEST").getOrNull()
    assertThat(retrievedRegion).isEqualTo(region)
  }

  @Test
  fun createArea() {
    val region = regionRepository.saveAndFlush(Region(code = "TESTREGION", description = "TESTREGION"))
    val area = Area(code = "TESTAREA", description = "TESTAREA", region = region)
    areaRepository.saveAndFlush(area)

    val retrievedArea = areaRepository.findById("TESTAREA").getOrNull()
    assertThat(retrievedArea).isEqualTo(area)
    assertThat(retrievedArea?.region).isEqualTo(region)
  }
}
