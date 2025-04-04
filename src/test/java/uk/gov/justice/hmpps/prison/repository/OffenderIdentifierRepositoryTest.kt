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
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
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

  @Autowired
  private lateinit var offenderRepository: OffenderRepository

  @Test
  @DisplayName("Can get offender identifiers by prisoner number")
  fun canGetOffenderIdentifiers() {
    val identifiers = repository.findOffenderIdentifiersByOffender_NomsId("A1234AL")
    assertThat(identifiers).hasSize(4)
  }

  @Test
  @DisplayName("Can move identifiers onto a new alias without any existing identifiers")
  fun moveIdentifiersToNewAlias() {
    assertThat(identifiersForOffenderId(-1020)).isNotEmpty()
    assertThat(identifiersForOffenderId(-1076)).isEmpty()

    repository.moveIdentifiersToNewAlias(-1020, -1076)
    commitAndStartNewTransaction()

    assertThat(identifiersForOffenderId(-1020)).isEmpty()
    assertThat(identifiersForOffenderId(-1076)).isNotEmpty()

    // and restore...
    repository.moveIdentifiersToNewAlias(-1076, -1020)
    commitAndStartNewTransaction()
  }

  private fun identifiersForOffenderId(offenderId: Long) = offenderRepository.findById(offenderId).get().identifiers

  private fun commitAndStartNewTransaction() {
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }
}
