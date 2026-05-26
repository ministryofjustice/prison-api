package uk.gov.justice.hmpps.prison.repository.v1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.sql.SQLException

class NomisV1SQLErrorCodeTranslatorTest {
  private val translator = NomisV1SQLErrorCodeTranslator()

  @Test
  fun customTranslate_errorCode() {
    val accessException = translator.customTranslate("hello", "sql", SQLException("reason", "state", 20040))
    assertThat(accessException)
      .hasMessage("Sum of sub account balances not equal to current balance")
  }

  @Test
  fun customTranslate_notMapped() {
    val accessException = translator.customTranslate("hello", "sql", SQLException("reason"))
    assertThat(accessException).isNull()
  }
}
