@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import java.time.LocalDate
import java.util.Optional

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderTransactionRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderTransactionRepository

  @Nested
  inner class GetNextTransactionId {
    @Test
    fun testGetNextTransactionId() {
      val transactionId = repository.getNextTransactionId()

      assertThat(transactionId).isEqualTo(1L)
    }
  }

  @Nested
  inner class FindById {
    @Test
    fun testOffenderTransactionMapping() {
      val optionalOffenderTransaction = repository.findById(OffenderTransactionId(301826802L, 1L))

      assertThat(optionalOffenderTransaction).get().extracting { it.prisonId }.isEqualTo("LEI")
    }
  }

  @Nested
  inner class FindAccountTransactions {
    @Test
    fun `no to date specified`() {
      val transactions = repository.findAccountTransactions(-1009, "LEI", "REG", LocalDate.parse("2019-01-01"), null)
      assertThat(transactions.map { it.clientUniqueRef }).hasSize(11)
      assertThat(transactions[9].clientUniqueRef).isEqualTo("mtp-prod-520832")
      assertThat(transactions[10].clientUniqueRef).isEqualTo("mtp-prod-520829")
    }

    @Test
    fun `to date specified`() {
      val transactions = repository.findAccountTransactions(
        -1009,
        "LEI",
        "REG",
        LocalDate.parse("2019-01-01"),
        LocalDate.parse("2019-10-19"),
      )
      assertThat(transactions.map { it.clientUniqueRef }).hasSize(1)
      assertThat(transactions[0].clientUniqueRef).isEqualTo("mtp-prod-520829")
    }
  }

  @Nested
  inner class FindAddHoldTransaction {
    @Test
    fun testFindHold() {
      val transaction = repository.findAddHoldTransactionForUpdate(rootOffenderId = -1009, agencyLocationId = "LEI", holdNumber = 5)

      assertThat(transaction.get().id.transactionId).isEqualTo(301826706)
    }

    @Test
    fun testGetHoldThatHasBeenReleased() {
      val transaction = repository.findAddHoldTransactionForUpdate(rootOffenderId = -1009, agencyLocationId = "LEI", holdNumber = 3)
      assertThat(transaction).isEqualTo(Optional.empty<OffenderTransaction>())
    }

    @Test
    fun testGetHoldThatDoesNotExist() {
      val transaction = repository.findAddHoldTransactionForUpdate(rootOffenderId = -1009, agencyLocationId = "LEI", holdNumber = -5)
      assertThat(transaction).isEqualTo(Optional.empty<OffenderTransaction>())
    }
  }
}
