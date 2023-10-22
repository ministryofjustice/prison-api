package uk.gov.justice.hmpps.prison.repository


import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gang

private const val NEW_GANG_CODE = "NEW_GANG"

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GangRepositoryTest {

  @Autowired
  lateinit var repository: GangRepository

  lateinit var gang: Gang

  @AfterEach
  fun teardown() {
    repository.delete(gang)
  }

  @BeforeEach
  fun setup() {
    gang = Gang(
      code = NEW_GANG_CODE,
      name = "The New Gang",
      parent = Gang(code = "PARENT_GANG", name = "Parent Gang")
    )
    repository.save(gang)
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun canRetrieveAGang() {
    val findAGang = repository.findById(NEW_GANG_CODE)
    assertThat(findAGang.isPresent).isTrue()
    assertThat(findAGang.get()).isEqualTo(gang)
  }
}