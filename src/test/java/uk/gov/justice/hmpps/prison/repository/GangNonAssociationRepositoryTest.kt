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
private const val NEW_GANG_CODE_3 = "NEW_GANG_3"

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
  lateinit var gang3: Gang

  @AfterEach
  fun teardown() {
    repository.deleteAll(repository.findAllByGangCode(primaryGang.code))
    repository.deleteAll(repository.findAllByGangCode(secondaryGang.code))
    repository.deleteAll(repository.findAllByGangCode(gang3.code))

    gangRepository.delete(primaryGang)
    gangRepository.delete(secondaryGang)
    gangRepository.delete(gang3)
  }

  @BeforeEach
  fun setup() {
    primaryGang = Gang(
      code = NEW_GANG_CODE_1,
      name = "The First Gang",
    )
    gangRepository.save(primaryGang)

    secondaryGang = Gang(
      code = NEW_GANG_CODE_2,
      name = "The Second Gang",
    )
    gangRepository.save(secondaryGang)

    gang3 = Gang(
      code = NEW_GANG_CODE_3,
      name = "The Third Gang",
    )
    gangRepository.save(gang3)

    repository.save(
      GangNonAssociation(
        primaryGang = primaryGang,
        secondaryGang = secondaryGang,
        nonAssociationReason = nonAssociationReasonRepository.findById(NonAssociationReason.pk("BUL")).orElseThrow(),
      ),
    )

    repository.save(
      GangNonAssociation(
        primaryGang = gang3,
        secondaryGang = secondaryGang,
        nonAssociationReason = nonAssociationReasonRepository.findById(NonAssociationReason.pk("RIV")).orElseThrow(),
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

  @Test
  fun canFindAGangNa() {
    TestTransaction.start()
    repository.findAllByGangCode(NEW_GANG_CODE_2).let {
      Assertions.assertThat(it).hasSize(2)
      Assertions.assertThat(it[0].primaryGang).isEqualTo(primaryGang)
      Assertions.assertThat(it[0].secondaryGang).isEqualTo(secondaryGang)
      Assertions.assertThat(it[0].nonAssociationReason.code).isEqualTo("BUL")

      Assertions.assertThat(it[1].primaryGang).isEqualTo(gang3)
      Assertions.assertThat(it[1].secondaryGang).isEqualTo(secondaryGang)
      Assertions.assertThat(it[1].nonAssociationReason.code).isEqualTo("RIV")
    }
  }
}
