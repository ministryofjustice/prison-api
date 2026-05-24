package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NomsIdSequenceTest {
  @Test
  fun isNextSequenceValidSimple() {
    val nomsIdSequence = NomsIdSequence.builder()
      .suffixAlphaSeq(27)
      .currentSuffix("AA")
      .currentPrefix("A")
      .prefixAlphaSeq(1)
      .nomsId(0)
      .build()
    assertThat(nomsIdSequence.prisonerIdentifier).isEqualTo("A0001AA")

    assertThat(nomsIdSequence.next().prisonerIdentifier).isEqualTo("A0002AA")
  }

  @Test
  fun isNextSequenceValidNextSuffix() {
    val nomsIdSequence = NomsIdSequence.builder()
      .suffixAlphaSeq(27)
      .currentSuffix("AA")
      .currentPrefix("A")
      .prefixAlphaSeq(1)
      .nomsId(9998)
      .build()
    assertThat(nomsIdSequence.prisonerIdentifier).isEqualTo("A9999AA")

    assertThat(nomsIdSequence.next().prisonerIdentifier).isEqualTo("A0001AC")
  }

  @Test
  fun isNextSequenceValidNextPrefix() {
    val nomsIdSequence = NomsIdSequence.builder()
      .suffixAlphaSeq(702)
      .currentSuffix("ZZ")
      .currentPrefix("A")
      .prefixAlphaSeq(1)
      .nomsId(9998)
      .build()
    assertThat(nomsIdSequence.prisonerIdentifier).isEqualTo("A9999ZZ")

    assertThat(nomsIdSequence.next().prisonerIdentifier).isEqualTo("C0001AA")
  }

  @Test
  fun isNextSequenceValidExcludedSuffix() {
    val nomsIdSequence = NomsIdSequence.builder()
      .suffixAlphaSeq(234)
      .currentSuffix("HZ")
      .currentPrefix("A")
      .prefixAlphaSeq(1)
      .nomsId(9998)
      .build()
    assertThat(nomsIdSequence.prisonerIdentifier).isEqualTo("A9999HZ")

    assertThat(nomsIdSequence.next().prisonerIdentifier).isEqualTo("A0001JA")
  }
}
