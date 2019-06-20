package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(description = "Bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Bookings {
    @ApiModelProperty(value = "Bookings", position = 0, allowEmptyValue = true)
    private List<Booking> bookings;
}
