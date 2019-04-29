package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "A charge which was made as part of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationCharge {

    @ApiModelProperty("Charge Id")
    private String oicChargeId;
    @ApiModelProperty("Offence Code")
    private String offenceCode;
    @ApiModelProperty("Offence Description")
    private String offenceDescription;
    @ApiModelProperty("Offence Finding Code")
    private String findingCode;
}
