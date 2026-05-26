package uk.gov.justice.hmpps.prison.service.curfews

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew
import java.time.LocalDate

class CurfewStateTest {
  @Test
  fun initialState() {
    assertThat(
      CurfewState.getState(
        HomeDetentionCurfew.builder
          ()
          .build(),
      ),
    )
      .isInstanceOf(InitialState::class.java)
  }

  @Test
  fun checksFailedState() {
    assertThat(
      CurfewState.getState(
        HomeDetentionCurfew.builder
          ()
          .passed(false)
          .checksPassedDate(LocalDate.now())
          .build(),
      ),
    ).isInstanceOf(ChecksFailedState::class.java)
  }

  @Test
  fun checksPassedState() {
    assertThat(
      CurfewState.getState(
        HomeDetentionCurfew.builder
          ()
          .passed(true)
          .checksPassedDate(LocalDate.now())
          .build(),
      ),
    ).isInstanceOf(ChecksPassedState::class.java)
  }

  @Test
  fun checksFailedRefusedState() {
    assertThat(
      CurfewState.getState(
        HomeDetentionCurfew.builder
          ()
          .passed(false)
          .checksPassedDate(LocalDate.now())
          .approvalStatus("REJECTED")
          .refusedReason("BREACH")
          .approvalStatusDate(LocalDate.now())
          .build(),
      ),
    ).isInstanceOf(ChecksFailedRefusedState::class.java)
  }

  @Test
  fun checksPassedRefusedState() {
    assertThat(
      CurfewState.getState(
        HomeDetentionCurfew.builder
          ()
          .passed(true)
          .checksPassedDate(LocalDate.now())
          .approvalStatus("REJECTED")
          .refusedReason("BREACH")
          .approvalStatusDate(LocalDate.now())
          .build(),
      ),
    ).isInstanceOf(ChecksPassedRefusedState::class.java)
  }

  @Test
  fun checksPassedApprovedState() {
    assertThat(
      CurfewState.getState(
        HomeDetentionCurfew.builder
          ()
          .passed(true)
          .checksPassedDate(LocalDate.now())
          .approvalStatus("APPROVED")
          .approvalStatusDate(LocalDate.now())
          .build(),
      ),
    ).isInstanceOf(ChecksPassedApprovedState::class.java)
  }
}
