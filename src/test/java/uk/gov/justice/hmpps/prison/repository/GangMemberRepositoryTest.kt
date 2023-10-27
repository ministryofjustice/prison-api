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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangMemberRepository

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GangBuilder::class)
class GangMemberRepositoryTest {

  @Autowired
  lateinit var repository: GangMemberRepository

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
  fun canFindGangMembers() {
    repository.findAllByBookingOffenderNomsIdAndGangActiveIsTrue("A1234AD").let {
      Assertions.assertThat(it).hasSize(1)
      Assertions.assertThat(it[0].gang.code).isEqualTo(NEW_GANG_CODE_2)
      Assertions.assertThat(it[0].booking.offender.nomsId).isEqualTo("A1234AD")
      Assertions.assertThat(it[0].commentText).isEqualTo("gang 2 - member 1 added")
      Assertions.assertThat(
        it[0].gang.getNonAssociations().let { naGangs ->
          Assertions.assertThat(naGangs).hasSize(3)
        },
      )
    }
  }
}
