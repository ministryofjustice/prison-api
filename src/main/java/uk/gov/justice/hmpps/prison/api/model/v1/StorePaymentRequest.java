package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "Transaction to Create")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "description", "amount", "client_transaction_id"})
public class StorePaymentRequest {

    @NotNull
    @Schema(description = "Valid payment type for the prison", example = "ADJ", allowableValues = "A_EARN,ADJ")
    private String type;

    @Size(max = 240)
    @Schema(description = "Description of the payment", example = "Adjustment")
    private String description;

    @NotNull
    @Schema(description = "Amount of the payment in pence, hence 1634 is £16.34", example = "1634")
    private Long amount;

    @NotNull
    @Size(max = 12)
    @Schema(description = "Client transaction identifier", example = "CL123212")
    @JsonProperty(value = "client_transaction_id")
    private String clientTransactionId;

    @JsonIgnore
    public BigDecimal getAmountInPounds() {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
    }

}
