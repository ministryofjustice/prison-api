package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApiModel(description = "Transfer to Savings Transaction")
@Data
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferTransaction {

    @NotNull(message = "The amount must be specified")
    @Min(value = 1, message = "The amount must be greater than 0")
    @ApiModelProperty(value = "Amount of transaction in pence, hence 1634 is £16.34", example = "1634", position = 1)
    private Long amount;

    @NotBlank(message = "The description must be specified")
    @Size(min = 1, max = 240, message = "The description must be between 1 and 240 characters")
    @ApiModelProperty(value = "Description of the Transaction", example = "Canteen Purchase of £16.34", position = 2)
    private String description;

    @NotBlank(message = "The client transaction ID must be specified")
    @Size(min = 1, max = 12, message = "The client transaction ID must be between 1 and 12 characters")
    @ApiModelProperty(value = "Client Transaction Id", example = "CL123212", position = 4)
    @JsonProperty(value = "client_transaction_id")
    private String clientTransactionId;

    @NotBlank(message = "The client unique reference must be specified")
    @Size(min = 1, max = 64, message = "The client unique reference must be between 1 and 64 characters")
    @Pattern(regexp = "[a-zA-Z0-9-_]+", message = "The client unique reference can only contain letters, numbers, hyphens and underscores")
    @JsonProperty(value = "client_unique_ref")
    @ApiModelProperty(value = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed", example = "CLIENT121131-0_11", position = 5)
    private String clientUniqueRef;

    @ApiIgnore
    @JsonIgnore
    public BigDecimal getAmountInPounds() {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
    }
}
