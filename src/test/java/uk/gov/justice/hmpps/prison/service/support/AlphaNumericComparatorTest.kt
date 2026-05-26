package uk.gov.justice.hmpps.prison.service.support

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SortAssertion(vararg input: String?) {
  private val actual: List<String?> = listOf(*input)

  fun sortsTo(vararg expected: String?) {
    assertThat(actual.sortedWith(AlphaNumericComparator())).isEqualTo(listOf(*expected))
  }
}

class AlphaNumericComparatorTest {
  @Test
  fun `should handle Just Letters`() {
    expectThat(
      "ZZ",
      "BB",
      "AA",
    ).sortsTo(
      "AA",
      "BB",
      "ZZ",
    )
  }

  @Test
  fun `should handle Just Numbers`() {
    expectThat(
      "33",
      "22",
      "11",
    ).sortsTo(
      "11",
      "22",
      "33",
    )
  }

  @Test
  fun `should handle WordsEndingWithNumbers`() {
    expectThat(
      "work shop 10",
      "work shop 12",
      "work shop 1",
      "work",
    ).sortsTo(
      "work",
      "work shop 1",
      "work shop 10",
      "work shop 12",
    )
  }

  @Test
  fun `should handle WordsStartingWithNumbers`() {
    expectThat(
      "work shop 10",
      "work shop 2",
      "WORK SHOP 3",
      "5-a-side",
      "aa",
    ).sortsTo(
      "5-a-side",
      "aa",
      "work shop 2",
      "WORK SHOP 3",
      "work shop 10",
    )
  }

  @Test
  fun `should handle NullWords`() {
    expectThat(
      "work shop 10",
      "work shop 2",
      null,
      "",
    ).sortsTo(
      null,
      "",
      "work shop 2",
      "work shop 10",
    )
  }

  @Test
  fun `should Work WithMixedSet`() {
    expectThat(
      "WORKSHOP 10",
      "WORKSHOP 2",
      "A",
      "bd2",
      "1test",
      "WORKSHOP 11",
      "WORKSHOP 0",
      "WORKSHOP 55",
      "1XS244R",
    ).sortsTo(
      "1test",
      "1XS244R",
      "A",
      "bd2",
      "WORKSHOP 0",
      "WORKSHOP 2",
      "WORKSHOP 10",
      "WORKSHOP 11",
      "WORKSHOP 55",
    )
  }

  @Test
  fun `should Work WithDoubleDigits`() {
    expectThat(
      "W 11",
      "W 2",
      "W 09",
      "W 3",
    ).sortsTo(
      "W 2",
      "W 3",
      "W 09",
      "W 11",
    )
  }

  @Test
  fun `should Work WithLocationsThatEndWithSymbol`() {
    expectThat(
      "WORKSHOP 0",
      "WORKSHOP (HDC)",
      "WORKSHOP (HDC)",
    ).sortsTo(
      "WORKSHOP (HDC)",
      "WORKSHOP (HDC)",
      "WORKSHOP 0",
    )
  }

  companion object {
    private fun expectThat(vararg input: String?): SortAssertion = SortAssertion(*input)
  }
}
