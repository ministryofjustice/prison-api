package uk.gov.justice.hmpps.prison.service.support

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StringWithAbbreviationsProcessorTest {
  @Test
  fun formatMultipleUniqueMatches() {
    // Assert that all abbreviations are unchanged when there are multiple unique ones in a string
    val abbreviationsString = "AAA, ADTP, AIC, AM, ATB, BBV, BHU, BICS, CAD, CASU, CES, CGL, CIT, CSC, CSCP"
    assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo(abbreviationsString)
  }

  @Test
  fun formatMultipleDuplicateMatches() {
    // Assert that all abbreviations are unchanged when there are multiple duplicate matches
    val abbreviationsString = "Test HMP HMP"
    assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo(abbreviationsString)
  }

  @Test
  fun formatWithinWord() {
    // Assert that it does not change characters if they're in the middle of words
    val abbreviationsString = "testHMPtest"
    assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString)).isEqualTo("Testhmptest")
  }

  @Test
  fun formatAbbreviationsWithFollowingNumbers() {
    // Assert that abbreviations that can have numbers after are uppercased
    val abbreviationsString = "Test hb3, hb11 and lb123 are converted but not lb or lrc1"
    assertThat(StringWithAbbreviationsProcessor.format(abbreviationsString))
      .isEqualTo("Test HB3, HB11 And LB123 Are Converted But Not Lb Or Lrc1")
  }

  @Test
  fun formatNull() {
    assertThat(StringWithAbbreviationsProcessor.format(null)).isNull()
  }
}
