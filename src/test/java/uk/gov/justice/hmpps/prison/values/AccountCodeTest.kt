package uk.gov.justice.hmpps.prison.values

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.values.AccountCode.Companion.byCodeName
import uk.gov.justice.hmpps.prison.values.AccountCode.Companion.exists

class AccountCodeTest {
  @Test
  fun accountCodeEnumHasCorrectCodeAndName() {
    assertThat(AccountCode.SAVINGS.code).isEqualTo("SAV")
    assertThat(AccountCode.SAVINGS.codeName).isEqualTo("savings")
    assertThat(AccountCode.SPENDS.code).isEqualTo("SPND")
    assertThat(AccountCode.SPENDS.codeName).isEqualTo("spends")
    assertThat(AccountCode.CASH.code).isEqualTo("REG")
    assertThat(AccountCode.CASH.codeName).isEqualTo("cash")
  }

  @Test
  fun byCodeNameReturnsCorrectCode() {
    assertThat(byCodeName("savings")!!.code).isEqualTo("SAV")
    assertThat(byCodeName("spends")!!.code).isEqualTo("SPND")
    assertThat(byCodeName("cash")!!.code).isEqualTo("REG")
    assertThat(byCodeName("unknown")).isNull()
  }

  @Test
  fun exists() {
    assertThat(exists("savings")).isTrue
    assertThat(exists("spends")).isTrue
    assertThat(exists("cash")).isTrue
    assertThat(exists("unknown")).isFalse
  }
}
