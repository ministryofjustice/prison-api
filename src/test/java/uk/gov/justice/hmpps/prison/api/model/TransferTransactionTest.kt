package uk.gov.justice.hmpps.prison.api.model

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.Test

class TransferTransactionTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun valid() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("desc")
          .clientTransactionId("clientTrans")
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .isEmpty()
  }

  @Test
  fun amount_missing() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .description("desc")
          .clientTransactionId("clientTrans")
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The amount must be specified")
  }

  @Test
  fun amount_0() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(0L)
          .description("desc")
          .clientTransactionId("clientTrans")
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The amount must be greater than 0")
  }

  @Test
  fun description_missing() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .clientTransactionId("clientTrans")
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The description must be specified")
  }

  @Test
  fun description_too_long() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("A".repeat(241))
          .clientTransactionId("clientTrans")
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The description must be between 1 and 240 characters")
  }

  @Test
  fun client_transaction_id_missing() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("desc")
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The client transaction ID must be specified")
  }

  @Test
  fun client_transaction_id_too_long() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("desc")
          .clientTransactionId("A".repeat(13))
          .clientUniqueRef("uniqueRef")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The client transaction ID must be between 1 and 12 characters")
  }

  @Test
  fun client_unique_reference_missing() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("desc")
          .clientTransactionId("clientTrans")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The client unique reference must be specified")
  }

  @Test
  fun client_unique_reference_too_long() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("desc")
          .clientTransactionId("clientTrans")
          .clientUniqueRef("A".repeat(65))
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The client unique reference must be between 1 and 64 characters")
  }

  @Test
  fun client_unique_reference_pattern() {
    assertThat(
      validator.validate(
        TransferTransaction.builder()
          .amount(12L)
          .description("desc")
          .clientTransactionId("clientTrans")
          .clientUniqueRef("a123 bc")
          .build(),
      ),
    )
      .hasSize(1)
      .extracting<String, RuntimeException>(ThrowingExtractor { it.message })
      .containsExactly("The client unique reference can only contain letters, numbers, hyphens and underscores")
  }
}
