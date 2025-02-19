package uk.gov.justice.hmpps.prison.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UnitConversionTest {

  @Test
  fun `test centimetres to feet and inches`() {
    val result = centimetresToFeetAndInches(182)
    assertThat(result).isEqualTo(Pair(6, 0))
  }

  @Test
  fun `test feet and inches to centimetres`() {
    val result = feetAndInchesToCentimetres(5, 10)
    assertThat(result).isEqualTo(178)
  }

  @Test
  fun `test kilograms to pounds`() {
    val result = kilogramsToPounds(70)
    assertThat(result).isEqualTo(154)
  }

  @Test
  fun `test kilograms to stone and pounds`() {
    val result = kilogramsToStoneAndPounds(70)
    assertThat(result).isEqualTo(Pair(11, 0))
  }

  @Test
  fun `test stone and pounds to kilograms`() {
    val result = stoneAndPoundsToKilograms(11, 0)
    assertThat(result).isEqualTo(70)
  }

  @Test
  fun `test feet and inches to centimetres with 18 inches`() {
    val result = feetAndInchesToCentimetres(0, 18)
    assertThat(result).isEqualTo(46)
  }

  @Test
  fun `test stone and pounds to kilograms with 90 pounds`() {
    val result = stoneAndPoundsToKilograms(0, 90)
    assertThat(result).isEqualTo(41)
  }
}
