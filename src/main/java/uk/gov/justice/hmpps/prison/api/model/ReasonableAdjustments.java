package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@ApiModel(description = "Reasonable Adjustments")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ReasonableAdjustments {

    @ApiModelProperty(value = "Reasonable Adjustments")
    List<ReasonableAdjustment> reasonableAdjustments;
}
