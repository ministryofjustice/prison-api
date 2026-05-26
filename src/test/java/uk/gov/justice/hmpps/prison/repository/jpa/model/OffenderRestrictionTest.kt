package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OffenderRestrictionTest {
  @Test
  fun isActive_startDate_today() {
    assertThat(OffenderRestriction.builder().startDate(LocalDate.now()).build().isActive).isTrue()
  }

  @Test
  fun isActive_startDate_tomorrow() {
    assertThat(OffenderRestriction.builder().startDate(LocalDate.now().plusDays(1)).build().isActive)
      .isFalse()
  }

  @Test
  fun isActive_with_expiryDate() {
    assertThat(
      OffenderRestriction.builder().startDate(LocalDate.now().minusDays(20)).expiryDate(LocalDate.now()).build()
        .isActive,
    ).isTrue()
  }

  @Test
  fun isActive_hasExpired() {
    assertThat(
      OffenderRestriction.builder().startDate(LocalDate.now().minusDays(20)).expiryDate(LocalDate.now().minusDays(1))
        .build().isActive,
    ).isFalse()
  }
}
