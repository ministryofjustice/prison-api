package uk.gov.justice.hmpps.prison.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneySupportTest {
  @Test
  fun `when amount is 10 then pence value is 1000`() {
    assertThat(MoneySupport.poundsToPence(BigDecimal("10.00"))).isEqualTo(1000)
  }

  @Test
  fun `when amount is 10_20 then pence value is 1020`() {
    assertThat(MoneySupport.poundsToPence(BigDecimal("10.20"))).isEqualTo(1020)
  }

  @Test
  fun `when amount is 20p only then pence value is 20`() {
    assertThat(MoneySupport.poundsToPence(BigDecimal("0.20"))).isEqualTo(20)
  }

  @Test
  fun `when pence is 1000 then pound value is 10_00`() {
    assertThat(MoneySupport.penceToPounds(1000L)).isEqualByComparingTo("10.00")
  }

  @Test
  fun `when pence is 1020 then pounds value is 10_20`() {
    assertThat(MoneySupport.penceToPounds(1020L)).isEqualByComparingTo("10.20")
  }
}
