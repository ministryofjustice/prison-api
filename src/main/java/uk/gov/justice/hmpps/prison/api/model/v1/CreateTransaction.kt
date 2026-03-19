package uk.gov.justice.hmpps.prison.api.model.v1

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.math.RoundingMode

@Schema(description = "Transaction to Create")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("type", "description", "amount", "client_transaction_id", "client_unique_ref")
data class CreateTransaction(
  @Schema(
    description = "Valid transaction type for the prison_id",
    example = "CANT",
    allowableValues = ["CANT", "REFND", "PHONE", "MRPR", "MTDS", "DTDS", "CASHD", "RELA", "RELS"],
    required = true,
  )
  val type: String,

  @Schema(description = "Description of the Transaction", example = "Canteen Purchase of £16.34", required = true)
  @field:Size(max = 240)
  val description: String,

  @Schema(description = "Amount of transaction in pence, hence 1634 is £16.34", example = "1634", required = true)
  val amount: Long,

  @Schema(description = "Client Transaction Id", example = "CL123212", required = true)
  @field:Size(max = 12)
  @JsonProperty(value = "client_transaction_id")
  val clientTransactionId: String,

  @JsonProperty(value = "client_unique_ref")
  @Schema(
    description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed",
    example = "CLIENT121131-0_11",
    required = true,
  )
  @field:Size(max = 64)
  @field:Pattern(regexp = "[a-zA-Z0-9-_]+")
  val clientUniqueRef: String,
) {
  @get:JsonIgnore
  val amountInPounds: BigDecimal
    get() = BigDecimal(amount).setScale(2, RoundingMode.HALF_UP)
      .divide(BigDecimal("100"), RoundingMode.HALF_UP)
}
