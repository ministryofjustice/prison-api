package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel(description = "Account Balance")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"spends", "savings", "cash"})
public class AccountBalance {

    @ApiModelProperty(value = "Cash balance", example = "13565")
    private Long cash;

    @ApiModelProperty(value = "Spends balance", example = "5678")
    private Long spends;

    @ApiModelProperty(value = "Saving balance", example = "12344")
    private Long savings;
}
