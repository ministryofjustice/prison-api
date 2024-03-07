package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountCodeRepositoryTest {
  @Autowired
  private lateinit var repository: AccountCodeRepository

  @Test
  fun testGetPrivateCashAccountCode() {
    val optionalAccountCode = repository.findByCaseLoadTypeAndSubAccountType("INST", "REG")
    assertThat(optionalAccountCode).get().extracting { it.accountCode }.isEqualTo(2101L)
  }

  @Test
  fun testGetSpendsAccountCode() {
    val optionalAccountCode = repository.findByCaseLoadTypeAndSubAccountType("INST", "SPND")
    assertThat(optionalAccountCode).get().extracting { it.accountCode }.isEqualTo(2102L)
  }

  @Test
  fun testGetSavingsAccountCode() {
    val optionalAccountCode = repository.findByCaseLoadTypeAndSubAccountType("INST", "SAV")
    assertThat(optionalAccountCode).get().extracting { it.accountCode }.isEqualTo(2103L)
  }
}
