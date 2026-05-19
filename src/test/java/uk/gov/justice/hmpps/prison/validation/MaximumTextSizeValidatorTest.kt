package uk.gov.justice.hmpps.prison.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSizeValidator
import java.util.stream.IntStream

class MaximumTextSizeValidatorTest {
  private val validator = MaximumTextSizeValidator()

  @Test
  fun testValidOnNullValue() {
    assertThat(validator.isValid(null, null)).isTrue()
  }

  @Test
  fun testValidWhen4000AnsiCharacters() {
    val testString: String = CHAR_TEXT_3990_BYTES + "ABCDE12345"
    assertThat(validator.isValid(testString, null)).isTrue()
  }

  @Test
  fun testInvalidWhen4001AnsiCharacters() {
    val testString: String = CHAR_TEXT_3990_BYTES + "ABCDE123450"
    assertThat(validator.isValid(testString, null)).isFalse()
  }

  @Test
  fun testInvalidWhenMoreThan4000BytesDueToUtf8() {
    val testString: String = CHAR_TEXT_3990_BYTES + "ABCDE12⌥⌘"
    assertThat(validator.isValid(testString, null)).isFalse()
  }

  companion object {
    private val CHAR_TEXT_3990_BYTES: String

    init {
      val stringWith10Chars = "ABCDE12345"
      val string = StringBuilder(3990)
      IntStream.rangeClosed(1, 399).forEach { string.append(stringWith10Chars) }
      CHAR_TEXT_3990_BYTES = string.toString()
    }
  }
}
