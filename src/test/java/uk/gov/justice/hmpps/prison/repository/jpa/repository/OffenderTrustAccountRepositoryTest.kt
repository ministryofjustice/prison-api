package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderTrustAccountRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderTrustAccountRepository

  @Test
  fun testOffenderTrustAccountMapping() {
    val optionalOffenderTrustAccount = repository.findById(OffenderTrustAccountId("LEI", -1001L)).get()

    assertThat(optionalOffenderTrustAccount.accountClosed).isFalse
  }
}
