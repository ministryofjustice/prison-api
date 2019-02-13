package net.syscon.elite.api.model.bulkappointments;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Repeat {
    @ApiModelProperty(required = true, value = "The period at which the appointment should repeat.", example = "WEEKLY", allowableValues = "DAILY, WEEKLY, FORTNIGHTLY, MONTHLY")
    @NotNull
    private RepeatPeriod repeatPeriod;

    @ApiModelProperty(required = true, value = "The number of times to repeat the appointments. Must be greater than 0", position = 1)
    @Min(1)
    @NotNull
    private Integer count;
}
