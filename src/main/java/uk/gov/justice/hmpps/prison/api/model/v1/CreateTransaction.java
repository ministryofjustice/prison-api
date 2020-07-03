package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApiModel(description = "Transaction to Create")
@Data
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "description", "amount", "client_transaction_id", "client_unique_ref"})
public class CreateTransaction {

    @NotNull
    @ApiModelProperty(value = "Valid transaction type for the prison_id", example = "CANT", allowableValues = "CANT,REFND,PHONE,MRPR,MTDS,DTDS,CASHD,RELA,RELS", position = 1)
    private String type;

    @Size(max = 240)
    @ApiModelProperty(value = "Description of the Transaction", example = "Canteen Purchase of £16.34", position = 2)
    private String description;

    @NotNull
    @ApiModelProperty(value = "Amount of transaction in pence, hence 1634 is £16.34", example = "1634", position = 3)
    private Long amount;

    @NotNull
    @Size(max = 12)
    @ApiModelProperty(value = "Client Transaction Id", example = "CL123212", position = 4)
    @JsonProperty(value = "client_transaction_id")
    private String clientTransactionId;

    @NotNull
    @Size(max = 64)
    @Pattern(regexp = "[a-zA-Z0-9-_]+")
    @JsonProperty(value = "client_unique_ref")
    @ApiModelProperty(value = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed", example = "CLIENT121131-0_11", position = 5)
    private String clientUniqueRef;

    @JsonIgnore
    public BigDecimal getAmountInPounds() {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
    }

}
