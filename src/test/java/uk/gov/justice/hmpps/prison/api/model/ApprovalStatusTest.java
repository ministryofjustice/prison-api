package uk.gov.justice.hmpps.prison.api.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.api.model.ApprovalStatus.builder;

public class ApprovalStatusTest {
    private static Validator validator;

    @BeforeAll
    public static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void notApprovalStatus() {
        assertThat(builder().build().isApproved()).isFalse();
    }

    @Test
    public void notApprovedStatus() {
        assertThat(builder().approvalStatus("XXX").build().isApproved()).isFalse();
    }

    @Test
    public void approvedStatus() {
        assertThat(builder().approvalStatus("APPROVED").build().isApproved()).isTrue();
    }

    @Test
    public void haRefusedReason() {
        assertThat(builder().refusedReason("X").build().hasRefusedReason()).isTrue();
    }

    @Test
    public void hasNoRefusedReason() {
        assertThat(builder().build().hasRefusedReason()).isFalse();
    }

    @Test
    public void validObjectApproved() {
        assertThat(validator.validate(
                builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.EPOCH)
                        .build())
        ).isEmpty();
    }

    @Test
    public void approvedWithRefusedReason() {
        assertThat(validator.validate(
                builder()
                        .approvalStatus("APPROVED")
                        .refusedReason("XXX")
                        .date(LocalDate.EPOCH)
                        .build())
        ).hasSize(1);
    }


    @Test
    public void noApprovalStatus() {
        assertThat(validator.validate(
                builder()
                        .date(LocalDate.EPOCH)
                        .build())
        ).hasSize(2);
    }

    @Test
    public void noDate() {
        assertThat(validator.validate(
                builder()
                        .approvalStatus("APPROVED")
                        .build())
        )
                .hasSize(1)
                .extracting("propertyPath").hasToString("[date]");
    }

    @Test
    public void noRefusalReasonWhenNotApproved() {
        assertThat(validator.validate(
                builder()
                        .approvalStatus("REFUSED")
                        .date(LocalDate.EPOCH)
                        .build())
        ).hasSize(1)
                .extracting("message").contains("A refusedReason is required when approval status is not 'APPROVED'.");
    }

    @Test
    public void validObjectNotApproved() {
        assertThat(validator.validate(
                builder()
                        .approvalStatus("REFUSED")
                        .refusedReason("XXX")
                        .date(LocalDate.EPOCH)
                        .build())
        ).isEmpty();
    }
}
