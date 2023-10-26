package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangRepository

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GangTestData::class)
class GangRepositoryTest {

  @Autowired
  lateinit var gangRepository: GangRepository

  @Autowired
  lateinit var gangTestData: GangTestData

  @BeforeEach
  fun setup() {
    gangTestData.initGangs()
  }

  @AfterEach
  internal fun tearDown() {
    gangTestData.teardown()
  }

  @Test
  fun canRetrieveAGang() {
    val findAGang = gangRepository.findById(NEW_GANG_CODE_1)
    assertThat(findAGang.isPresent).isTrue()
    assertThat(findAGang.get().code).isEqualTo(NEW_GANG_CODE_1)
  }
}
