package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.helper.builder.GangBuilder
import uk.gov.justice.hmpps.prison.helper.builder.NEW_GANG_CODE_1
import uk.gov.justice.hmpps.prison.helper.builder.NEW_GANG_CODE_3

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GangServiceImplIntTest {

  @Autowired
  lateinit var gangService: GangService

  @Autowired
  lateinit var gangBuilder: GangBuilder

  @BeforeEach
  internal fun setup() {
    gangBuilder.initGangs()
  }

  @AfterEach
  internal fun tearDown() {
    gangBuilder.teardown()
  }

  @Test
  fun getNonAssociatesInGangs() {
    val gangNas = gangService.getNonAssociatesInGangs("A1234AD")
    Assertions.assertThat(gangNas.gangNonAssociations).hasSize(2)
    Assertions.assertThat(
      gangNas.gangNonAssociations.map {
        it.code
      },
    ).containsExactlyInAnyOrder(NEW_GANG_CODE_1, NEW_GANG_CODE_3)
    Assertions.assertThat(
      gangNas.gangNonAssociations.flatMap { nonAssociationSummary ->
        nonAssociationSummary.members.map {
          it.offenderNo
        }
      },
    ).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AC", "A1234AE", "A1234AF")
  }
}
