package uk.gov.justice.hmpps.prison.api.model.adjudications;

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

    @ApiModelProperty(value = "Charge Id", example = "1506763/1")
    private String oicChargeId;
    @ApiModelProperty(value = "Offence Code", example = "51:22")
    private String offenceCode;
    @ApiModelProperty(value = "Offence Description", example = "Disobeys any lawful order")
    private String offenceDescription;
    @ApiModelProperty(value = "Offence Finding Code", example = "PROVED")
    private String findingCode;
}
