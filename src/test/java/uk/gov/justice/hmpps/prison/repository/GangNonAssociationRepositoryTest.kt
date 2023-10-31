package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.helper.builder.GangBuilder
import uk.gov.justice.hmpps.prison.helper.builder.NEW_GANG_CODE_2
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangNonAssociationRepository

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GangBuilder::class)
class GangNonAssociationRepositoryTest {

  @Autowired
  lateinit var repository: GangNonAssociationRepository

  @Autowired
  lateinit var gangBuilder: GangBuilder

  @BeforeEach
  fun setup() {
    gangBuilder.initGangs()
  }

  @AfterEach
  internal fun tearDown() {
    gangBuilder.teardown()
  }

  @Test
  fun canFindAGangNa() {
    repository.findAllByGangCode(NEW_GANG_CODE_2).let {
      Assertions.assertThat(it).hasSize(3)
    }
  }
}
