package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "Transaction to Create")
@Data
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "description", "amount", "client_transaction_id", "client_unique_ref"})
public class CreateTransaction {

    @NotNull
    @Schema(description = "Valid transaction type for the prison_id", example = "CANT", allowableValues = {"CANT","REFND","PHONE","MRPR","MTDS","DTDS","CASHD","RELA","RELS"})
    private String type;

    @Size(max = 240)
    @Schema(description = "Description of the Transaction", example = "Canteen Purchase of £16.34")
    private String description;

    @NotNull
    @Schema(description = "Amount of transaction in pence, hence 1634 is £16.34", example = "1634")
    private Long amount;

    @NotNull
    @Size(max = 12)
    @Schema(description = "Client Transaction Id", example = "CL123212")
    @JsonProperty(value = "client_transaction_id")
    private String clientTransactionId;

    @NotNull
    @Size(max = 64)
    @Pattern(regexp = "[a-zA-Z0-9-_]+")
    @JsonProperty(value = "client_unique_ref")
    @Schema(description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed", example = "CLIENT121131-0_11")
    private String clientUniqueRef;

    @JsonIgnore
    public BigDecimal getAmountInPounds() {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
    }

}
