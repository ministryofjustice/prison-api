package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OffenderCourtCaseTest {
  @Test
  fun case_is_not_active_by_default() {
    assertThat(OffenderCourtCase.builder().build().isActive).isFalse()
  }

  @Test
  fun case_is_not_active() {
    assertThat(OffenderCourtCase.builder().caseStatus(CaseStatus("I", "not active")).build().isActive)
      .isFalse()
  }

  @Test
  fun case_is_active() {
    assertThat(OffenderCourtCase.builder().caseStatus(CaseStatus("A", "active")).build().isActive).isTrue()
  }
}
