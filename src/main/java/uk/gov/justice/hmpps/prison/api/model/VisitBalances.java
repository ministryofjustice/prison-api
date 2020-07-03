package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Balances of visit orders and privilege visit orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitBalances {

    @ApiModelProperty(required = true, value = "Balance of visit orders remaining")
    private Integer remainingVo;

    @ApiModelProperty(required = true, value = "Balance of privilege visit orders remaining")
    public Integer remainingPvo;
}
