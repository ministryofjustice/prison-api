package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@ApiModel(description = "Offender damage obligation response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OffenderDamageObligationResponse {

    @ApiModelProperty(value = "List of offender damage obligations", position = 1)
    private List<OffenderDamageObligationModel> damageObligations;
}
