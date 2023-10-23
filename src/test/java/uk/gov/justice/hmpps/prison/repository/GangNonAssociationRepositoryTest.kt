package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gang
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociation
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociationId
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangNonAssociationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository

private const val NEW_GANG_CODE_1 = "NEW_GANG_1"
private const val NEW_GANG_CODE_2 = "NEW_GANG_2"

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GangNonAssociationRepositoryTest {

  @Autowired
  lateinit var gangRepository: GangRepository

  @Autowired
  lateinit var repository: GangNonAssociationRepository

  @Autowired
  lateinit var nonAssociationReasonRepository: ReferenceCodeRepository<NonAssociationReason>

  lateinit var primaryGang: Gang

  lateinit var secondaryGang: Gang

  @AfterEach
  fun teardown() {
    repository.deleteById(GangNonAssociationId(primaryGang, secondaryGang))
    gangRepository.delete(primaryGang)
    gangRepository.delete(secondaryGang)
  }

  @BeforeEach
  fun setup() {
    primaryGang = Gang(
      code = NEW_GANG_CODE_1,
      name = "The New Gang",
    )
    gangRepository.save(primaryGang)

    secondaryGang = Gang(
      code = NEW_GANG_CODE_2,
      name = "The New Gang",
    )
    gangRepository.save(secondaryGang)

    repository.save(
      GangNonAssociation(
        primaryGang = primaryGang,
        secondaryGang = secondaryGang,
        nonAssociationReason = nonAssociationReasonRepository.findById(NonAssociationReason.pk("BUL")).orElseThrow(),
      ),
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun canRetrieveAGangNa() {
    val findAGangNa = repository.findById(GangNonAssociationId(primaryGang, secondaryGang)).orElseThrow()
    Assertions.assertThat(findAGangNa.primaryGang).isEqualTo(primaryGang)
    Assertions.assertThat(findAGangNa.secondaryGang).isEqualTo(secondaryGang)
    Assertions.assertThat(findAGangNa.nonAssociationReason.code).isEqualTo("BUL")
  }
}
