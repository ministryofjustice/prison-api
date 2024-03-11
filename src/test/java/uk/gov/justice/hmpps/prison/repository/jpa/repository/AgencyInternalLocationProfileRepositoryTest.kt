package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AgencyInternalLocationProfileRepositoryTest {
  @Autowired
  private lateinit var repository: AgencyInternalLocationProfileRepository

  @Test
  fun findAllByLivingUnitAndAgencyIdAndDescription() {
    val expected = listOf(
      AgencyInternalLocationProfile.builder()
        .locationId(-3L)
        .code("DO")
        .profileType("HOU_UNIT_ATT")
        .housingAttributeReferenceCode(HousingAttributeReferenceCode("DO", "Double Occupancy"))
        .build(),
      AgencyInternalLocationProfile.builder()
        .locationId(-3L)
        .code("LC")
        .profileType("HOU_UNIT_ATT")
        .housingAttributeReferenceCode(HousingAttributeReferenceCode("LC", "Listener Cell"))
        .build(),
    )
    val profiles = repository.findAllByLocationId(-3L)
    assertThat(profiles).isEqualTo(expected)
  }
}
