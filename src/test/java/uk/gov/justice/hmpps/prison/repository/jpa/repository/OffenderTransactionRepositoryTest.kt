@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderTransactionRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderTransactionRepository

  @Test
  fun testGetNextTransactionId() {
    val transactionId = repository.getNextTransactionId()

    assertThat(transactionId).isEqualTo(1L)
  }

  @Test
  fun testOffenderTransactionMapping() {
    val optionalOffenderTransaction = repository.findById(OffenderTransaction.Pk(301826802L, 1L))

    assertThat(optionalOffenderTransaction).get().extracting { it.prisonId }.isEqualTo("LEI")
  }

  @Nested
  inner class findAccountTransactions {
    @Test
    fun `no to date specified`() {
      val transactions = repository.findAccountTransactions(-1009, "LEI", "REG", LocalDate.parse("2019-01-01"), null)
      assertThat(transactions.map { it.clientUniqueRef }).hasSize(2).containsOnly("mtp-prod-520829", "mtp-prod-520832")
    }

    @Test
    fun `to date specified`() {
      val transactions = repository.findAccountTransactions(-1009, "LEI", "REG", LocalDate.parse("2019-01-01"), LocalDate.parse("2019-10-19"))
      assertThat(transactions.map { it.clientUniqueRef }).hasSize(1).containsOnly("mtp-prod-520829")
    }
  }
}
