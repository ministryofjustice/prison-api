package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Cell swap result")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CellSwapResult {
    @ApiModelProperty(required = true, value = "Unique, numeric booking id.", position = 1, example = "1234134")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Identifier of agency that offender is associated with.", position = 2, example = "MDI")
    private String agencyId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that offender is assigned to.", position = 3, example = "123123")
    private Long assignedLivingUnitId;

    @ApiModelProperty(value = "Description of living unit (e.g. cell) that offender is assigned to.", position = 4, example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;
}
