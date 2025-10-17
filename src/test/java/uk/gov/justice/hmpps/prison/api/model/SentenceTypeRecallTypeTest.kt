package uk.gov.justice.hmpps.prison.api.model

import org.junit.Test

class SentenceTypeRecallTypeTest {

  @Test
  fun test() {
    SentenceTypeRecallType.entries.forEach {
      if (it.name != it.sentenceType) {
        println("${it.name}(${it.sentenceType})")
      }
    }
  }
}
