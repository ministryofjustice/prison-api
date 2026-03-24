package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class FinanceRepositoryTest {
  @Autowired
  private lateinit var repository: FinanceRepository

  @Nested
  inner class GetBalances {
    @Test
    fun testGetAccount() {
      val account = repository.getBalances(-1L, "LEI")
      assertThat(account).isNotNull()
      assertThat(account.cash).isEqualByComparingTo("1.24")
      assertThat(account.spends).isEqualByComparingTo("2.50")
      assertThat(account.savings).isEqualByComparingTo("200.50")
    }

    @Test
    fun testGetAccountInvalidBookingId() {
      val account = repository.getBalances(1001L, "LEI")
      assertThat(account).isNotNull()
      assertThat(account.cash).isNull()
      assertThat(account.spends).isNull()
      assertThat(account.savings).isNull()
    }

    @Test
    fun testWherePrisonerHasNoAccountInThatAgency() {
      val account = repository.getBalances(-1L, "BXI")
      assertThat(account).isNotNull()
      assertThat(account.cash).isNull()
      assertThat(account.spends).isNull()
      assertThat(account.savings).isNull()
    }

    @Test
    fun testWherePrisonerHasSpendsInDifferentPrison() {
      val account = repository.getBalances(-1L, "MDI")
      assertThat(account).isNotNull()
      assertThat(account.spends).isEqualByComparingTo("12.75")
      assertThat(account.cash).isEqualByComparingTo("0.00")
      assertThat(account.savings).isEqualByComparingTo("0.00")
    }
  }
}
