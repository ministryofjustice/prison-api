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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPhoneRepository
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
@DisplayName("OffenderPhoneRepository")
class OffenderPhoneRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderPhoneRepository

  @Test
  @DisplayName("Can get offender phones by prisoner number and phone ID")
  fun canGetPhoneNumberForOffender() {
    val phoneNumber = repository.findByRootNomsIdAndPhoneId(PRISONER_NUMBER, PhoneOne.ID)
    assertThat(phoneNumber).isNotNull()
    assertThat(phoneNumber.get().phoneNo).isEqualTo(PhoneOne.NUMBER)
    assertThat(phoneNumber.get().phoneType).isEqualTo(PhoneOne.TYPE)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AI"

    data object PhoneOne {
      const val TYPE = "HOME"
      const val NUMBER = "0114 878787"
      const val ID = -16L
    }
  }
}
