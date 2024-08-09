package uk.gov.justice.hmpps.prison.repository

import lombok.extern.slf4j.Slf4j
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
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
@DisplayName("OffenderIdentifierRepository")
class OffenderIdentifierRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderIdentifierRepository

  @Test
  @DisplayName("Can get offender identifiers")
  fun canGetOffenderIdentifiers() {
    val identifiers = repository.findOffenderIdentifiersByOffender_NomsId("A1234AL")
    Assertions.assertThat(identifiers).hasSize(4)
  }
}
