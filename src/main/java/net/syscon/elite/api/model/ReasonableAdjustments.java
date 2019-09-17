package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
