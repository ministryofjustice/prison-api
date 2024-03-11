package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class AgencyLocationRepositoryTest {
  @Autowired
  private lateinit var repository: AgencyLocationRepository

  @Test
  fun findAgenciesWithActiveFlag_returnsAllActiveAgencies() {
    val expected = AgencyLocationFilter.builder()
      .build()
    val agencies = repository.findAll(expected)
    assertThat(agencies).extracting("id")
      .containsAnyOf("LEI", "ABDRCT", "BMI", "BXI", "COURT1", "MDI", "MUL", "RNI", "SYI", "TRO", "WAI")
  }

  @Test
  fun findAgenciesWithInactiveFlag_returnsAllInactiveAgencies() {
    val expected = AgencyLocationFilter.builder()
      .active(false)
      .build()
    val agencies = repository.findAll(expected)
    assertThat(agencies).extracting("id").containsExactlyInAnyOrder("ZZGHI")
  }

  @Test
  fun findAgenciesByAgency_returnsAgency() {
    val expected = AgencyLocationFilter.builder()
      .id("LEI")
      .build()
    val agencies = repository.findAll(expected)
    assertThat(agencies).extracting("id").containsExactlyInAnyOrder("LEI")
  }

  @Test
  fun findAgenciesByAgencyNotActive_returnsNoAgency() {
    val expected = AgencyLocationFilter.builder()
      .id("LEI")
      .active(false)
      .build()
    val agencies = repository.findAll(expected)
    assertThat(agencies).extracting("id").isEmpty()
  }

  @Test
  fun findAgenciesIncludingOUTandTRN_returnsAll() {
    val expected = AgencyLocationFilter.builder()
      .active(true)
      .excludedAgencies(null)
      .build()
    val agencies = repository.findAll(expected)
    assertThat(agencies).extracting("id")
      .containsAnyOf("TRN", "OUT", "LEI", "ABDRCT", "BMI", "BXI", "COURT1", "MDI", "MUL", "RNI", "SYI", "TRO", "WAI")
  }

  @Test
  fun createAnAgency() {
    val newAgency = AgencyLocation.builder()
      .id("TEST")
      .description("A Test Agency")
      .active(true)
      .type(AgencyLocationType.PRISON_TYPE)
      .build()
    val createdAgency = repository.save(newAgency)
    val retrievedAgency = repository.findById("TEST").orElseThrow()
    assertThat(retrievedAgency).isEqualTo(createdAgency)
    assertThat(retrievedAgency).extracting("createUserId").isEqualTo("user")
  }
}
