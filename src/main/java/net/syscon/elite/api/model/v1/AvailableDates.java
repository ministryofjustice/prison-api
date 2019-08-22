package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Available Dates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AvailableDates {

    @ApiModelProperty(value = "Available Dates", allowEmptyValue = true)
    private List<LocalDate> dates;
}
