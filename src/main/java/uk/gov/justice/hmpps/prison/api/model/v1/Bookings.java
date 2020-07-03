package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
