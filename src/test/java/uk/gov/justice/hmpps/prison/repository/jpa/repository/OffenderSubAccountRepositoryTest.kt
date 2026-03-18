package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import java.math.BigDecimal
import java.util.Optional

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderSubAccountRepositoryTest {
  @Autowired
  private val repository: OffenderSubAccountRepository? = null

  @Test
  fun testOffenderSubAccountMapping() {
    val optionalOffenderSubAccount: Optional<OffenderSubAccount?> =
      repository!!.findById(OffenderSubAccountId("LEI", -1001L, 2101L))

    Assertions.assertThat<OffenderSubAccount>(optionalOffenderSubAccount).get()
      .extracting<BigDecimal>(OffenderSubAccount::balance)
      .usingComparator(Comparator { obj: BigDecimal?, `val`: BigDecimal? -> obj!!.compareTo(`val`) })
      .isEqualTo(BigDecimal("1.24"))
  }
}
