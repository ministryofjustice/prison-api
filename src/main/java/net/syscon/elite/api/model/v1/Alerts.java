package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(description = "Alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Alerts {
    @ApiModelProperty(value = "Alerts", allowEmptyValue = true)
    private List<AlertV1> alerts;
}
