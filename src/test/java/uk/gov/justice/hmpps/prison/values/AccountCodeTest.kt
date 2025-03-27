package uk.gov.justice.hmpps.prison.values

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.values.AccountCode.byCode
import uk.gov.justice.hmpps.prison.values.AccountCode.byCodeName
import uk.gov.justice.hmpps.prison.values.AccountCode.codeForNameOrEmpty

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
    assertThat(byCodeName("savings").map { it.code }).get().isEqualTo("SAV")
    assertThat(byCodeName("spends").map { it.code }).get().isEqualTo("SPND")
    assertThat(byCodeName("cash").map { it.code }).get().isEqualTo("REG")
    assertThat(byCodeName("unknown").map { it.code }).isEmpty
  }

  @Test
  fun byCodeReturnsCorrectCodeName() {
    assertThat(byCode("SAV").map { it.codeName }).get().isEqualTo("savings")
    assertThat(byCode("SPND").map { it.codeName }).get().isEqualTo("spends")
    assertThat(byCode("REG").map { it.codeName }).get().isEqualTo("cash")
    assertThat(byCode("unknown").map { it.codeName }).isEmpty
  }

  @Test
  fun codeForNameOrEmptyReturnsCode() {
    assertThat(codeForNameOrEmpty("cash")).get().isEqualTo("REG")
    assertThat(codeForNameOrEmpty("spends")).get().isEqualTo("SPND")
    assertThat(codeForNameOrEmpty("savings")).get().isEqualTo("SAV")
    assertThat(codeForNameOrEmpty("unknown")).isEmpty
  }
}
