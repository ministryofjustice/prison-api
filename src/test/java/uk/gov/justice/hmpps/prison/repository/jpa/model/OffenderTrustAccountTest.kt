package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OffenderTrustAccountTest {
  @Test
  fun isAccountClosed_Y_is_true() {
    val offenderTrustAccount = OffenderTrustAccount(OffenderTrustAccountId("ASI", 1), true)
    assertThat(offenderTrustAccount.accountClosed).isTrue()
  }

  @Test
  fun isAccountClosed_N_is_false() {
    val offenderTrustAccount = OffenderTrustAccount(OffenderTrustAccountId("ASI", 1), false)
    assertThat(offenderTrustAccount.accountClosed).isFalse()
  }
}
