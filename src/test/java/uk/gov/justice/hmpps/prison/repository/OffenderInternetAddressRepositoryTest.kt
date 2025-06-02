package uk.gov.justice.hmpps.prison.repository

import lombok.extern.slf4j.Slf4j
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderInternetAddressRepository
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
  HmppsAuthenticationHolder::class,
  AuditorAwareImpl::class,
  PersistenceConfigs::class,
)
@Slf4j
@DisplayName("OffenderInternetAddressRepository")
class OffenderInternetAddressRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderInternetAddressRepository

  @Test
  @DisplayName("Can get offender internet addresses by prisoner number and address ID")
  fun canGetInternetAddressesForOffender() {
    val internetAddress = repository.findByRootNomsIdAndInternetAddressId(PRISONER_NUMBER, AddressOne.ID)
    assertThat(internetAddress).isNotNull()
    assertThat(internetAddress.get().internetAddress).isEqualTo(AddressOne.INTERNET_ADDRESS)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1238AI"

    data object AddressOne {
      const val ID = -10L
      const val INTERNET_ADDRESS = "prisoner@home.com"
    }
  }
}
