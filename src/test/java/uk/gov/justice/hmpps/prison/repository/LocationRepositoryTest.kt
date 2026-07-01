package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class LocationRepositoryTest(
  @Autowired private val repository: LocationRepository,
) {
  @Test
  fun findLocationIdWithFilter() {
    val activeAgencyLocationId = -1L

    assertThat(repository.findLocation(activeAgencyLocationId, StatusFilter.ALL)).isPresent()
    assertThat(repository.findLocation(activeAgencyLocationId, StatusFilter.INACTIVE_ONLY))
      .isEmpty()
    assertThat(repository.findLocation(activeAgencyLocationId, StatusFilter.ACTIVE_ONLY))
      .isPresent()

    val inactiveAgencyLocationId = -31L

    assertThat(repository.findLocation(inactiveAgencyLocationId, StatusFilter.ALL)).isPresent()
    assertThat(repository.findLocation(inactiveAgencyLocationId, StatusFilter.INACTIVE_ONLY))
      .isPresent()
    assertThat(repository.findLocation(inactiveAgencyLocationId, StatusFilter.ACTIVE_ONLY))
      .isEmpty()
  }
}
