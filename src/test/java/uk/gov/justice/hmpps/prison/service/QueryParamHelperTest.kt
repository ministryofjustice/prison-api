package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry.entry
import org.junit.jupiter.api.Test

class QueryParamHelperTest {
  @Test
  fun splitTypes_empty() {
    val map = QueryParamHelper.splitTypes(mutableListOf<String>())
    assertThat(map).isEmpty()
  }

  @Test
  fun splitTypes() {
    val map = QueryParamHelper.splitTypes(mutableListOf("BOB+JOE", "BOB+FRED", "HARRY", "JOHN+SMITH"))
    assertThat(map).containsKeys("BOB", "HARRY", "JOHN").containsExactly(
      entry("BOB", mutableListOf("JOE", "FRED")),
      entry("HARRY", mutableListOf()),
      entry("JOHN", mutableListOf("SMITH")),
    )
  }
}
