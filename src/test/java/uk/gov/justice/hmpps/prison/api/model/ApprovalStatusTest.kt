package uk.gov.justice.hmpps.prison.api.model

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApprovalStatusTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun notApprovalStatus() {
    assertThat(ApprovalStatus.builder().build().isApproved).isFalse()
  }

  @Test
  fun notApprovedStatus() {
    assertThat(ApprovalStatus.builder().approvalStatus("XXX").build().isApproved).isFalse()
  }

  @Test
  fun approvedStatus() {
    assertThat(ApprovalStatus.builder().approvalStatus("APPROVED").build().isApproved).isTrue()
  }

  @Test
  fun haRefusedReason() {
    assertThat(ApprovalStatus.builder().refusedReason("X").build().hasRefusedReason()).isTrue()
  }

  @Test
  fun hasNoRefusedReason() {
    assertThat(ApprovalStatus.builder().build().hasRefusedReason()).isFalse()
  }

  @Test
  fun validObjectApproved() {
    assertThat(
      validator.validate(
        ApprovalStatus.builder()
          .approvalStatus("APPROVED")
          .date(LocalDate.EPOCH)
          .build(),
      ),
    ).isEmpty()
  }

  @Test
  fun approvedWithRefusedReason() {
    assertThat(
      validator.validate(
        ApprovalStatus.builder()
          .approvalStatus("APPROVED")
          .refusedReason("XXX")
          .date(LocalDate.EPOCH)
          .build(),
      ),
    ).hasSize(1)
  }

  @Test
  fun noApprovalStatus() {
    assertThat(
      validator.validate(
        ApprovalStatus.builder()
          .date(LocalDate.EPOCH)
          .build(),
      ),
    ).hasSize(2)
  }

  @Test
  fun noDate() {
    assertThat(
      validator.validate(
        ApprovalStatus.builder()
          .approvalStatus("APPROVED")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting("propertyPath").hasToString("[date]")
  }

  @Test
  fun noRefusalReasonWhenNotApproved() {
    assertThat(
      validator.validate(
        ApprovalStatus.builder()
          .approvalStatus("REFUSED")
          .date(LocalDate.EPOCH)
          .build(),
      ),
    ).hasSize(1)
      .extracting("message").contains("A refusedReason is required when approval status is not 'APPROVED'.")
  }

  @Test
  fun validObjectNotApproved() {
    assertThat(
      validator.validate(
        ApprovalStatus.builder()
          .approvalStatus("REFUSED")
          .refusedReason("XXX")
          .date(LocalDate.EPOCH)
          .build(),
      ),
    ).isEmpty()
  }
}
