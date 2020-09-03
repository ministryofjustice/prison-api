package uk.gov.justice.hmpps.prison.api.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferTransactionTest {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void valid() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("desc")
                .clientTransactionId("clientTrans")
                .clientUniqueRef("uniqueRef")
                .build()))
                .isEmpty();
    }

    @Test
    public void amount_missing() {
        assertThat(validator.validate(TransferTransaction.builder()
                .description("desc")
                .clientTransactionId("clientTrans")
                .clientUniqueRef("uniqueRef")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The amount must be specified");
    }

    @Test
    public void amount_0() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(0L)
                .description("desc")
                .clientTransactionId("clientTrans")
                .clientUniqueRef("uniqueRef")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The amount must be greater than 0");
    }

    @Test
    public void description_missing() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .clientTransactionId("clientTrans")
                .clientUniqueRef("uniqueRef")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The description must be specified");
    }

    @Test
    public void description_too_long() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("A".repeat(241))
                .clientTransactionId("clientTrans")
                .clientUniqueRef("uniqueRef")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The description must be between 1 and 240 characters");
    }

    @Test
    public void client_transaction_id_missing() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("desc")
                .clientUniqueRef("uniqueRef")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The client transaction ID must be specified");
    }

    @Test
    public void client_transaction_id_too_long() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("desc")
                .clientTransactionId("A".repeat(13))
                .clientUniqueRef("uniqueRef")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The client transaction ID must be between 1 and 12 characters");
    }

    @Test
    public void client_unique_reference_missing() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("desc")
                .clientTransactionId("clientTrans")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The client unique reference must be specified");
    }

    @Test
    public void client_unique_reference_too_long() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("desc")
                .clientTransactionId("clientTrans")
                .clientUniqueRef("A".repeat(65))
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The client unique reference must be between 1 and 64 characters");
    }

    @Test
    public void client_unique_reference_pattern() {
        assertThat(validator.validate(TransferTransaction.builder()
                .amount(12L)
                .description("desc")
                .clientTransactionId("clientTrans")
                .clientUniqueRef("a123 bc")
                .build()))
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage).containsExactly("The client unique reference can only contain letters, numbers, hyphens and underscores");
    }
}
