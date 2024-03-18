package uk.gov.justice.hmpps.prison.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OptionalUtilTest {
  @Test
  fun orNullReturnsValueIfNotNull() {
    val id = "abc"
    val instance = DummyClass(clazz = DummyClass(id = id))
    assertThat(
      OptionalUtil.getOrNull<DummyClass, String>(instance.clazz) { it.id },
    ).isEqualTo(id)
  }

  @Test
  fun orNullReturnsNullIfReferenceIsNull() {
    val instance = DummyClass()
    assertThat(
      OptionalUtil.getOrNull<DummyClass, String>(instance.clazz) { it.id },
    ).isNull()
  }

  data class DummyClass(val clazz: DummyClass? = null, val id: String? = null)
}
