package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Available Dates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AvailableDates {

    @ApiModelProperty(value = "Available Dates", dataType = "[Ljava.sql.Date;", allowEmptyValue = true)
    private List<LocalDate> dates;
}
