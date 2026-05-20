package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@WithMockAuthUser("anonymous")
class AgencyLocationRepositoryTxTest(
  @Autowired private val repository: AgencyLocationRepository,
  @Autowired private val agencyLocationTypeReferenceCode: ReferenceCodeRepository<AgencyLocationType>,
) {
  @Test
  fun testPersistingAgency() {
    val newAgency = AgencyLocation.builder()
      .id(TEST_AGY_ID)
      .description("A Test Agency")
      .active(true)
      .type(agencyLocationTypeReferenceCode.findById(AgencyLocationType.CRT).orElseThrow())
      .build()

    val persistedEntity = repository.save(newAgency)

    TestTransaction.flagForCommit()
    TestTransaction.end()

    assertThat(persistedEntity.getId()).isNotNull()

    TestTransaction.start()

    val retrievedAgency = repository.findById(TEST_AGY_ID).orElseThrow()
    assertThat(retrievedAgency).isEqualTo(persistedEntity)

    // Check that the user name and timestamp is set
    val createUserId = ReflectionTestUtils.getField(retrievedAgency, AgencyLocation::class.java, "createUserId")
    assertThat(createUserId).isEqualTo("anonymous")

    val createDatetime = ReflectionTestUtils.getField(retrievedAgency, AgencyLocation::class.java, "createDatetime")
    assertThat(createDatetime).isNotNull()

    // Clean up
    repository.delete(retrievedAgency)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    assertThat(repository.findById(TEST_AGY_ID)).isEmpty()
  }

  companion object {
    const val TEST_AGY_ID: String = "TEST"
  }
}
