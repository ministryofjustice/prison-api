package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@ApiModel(description = "Represents a court hearing for an offender court case.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtHearing {

    @ApiModelProperty(value = "The court hearing identifier.", position = 1, example = "123456789")
    private Long id;

    @ApiModelProperty(value = "The date of the court hearing.", position = 2, example = "2020-03-01")
    private LocalDate date;

    @ApiModelProperty(value = "The time of the court hearing", position = 3, example = "12:00")
    private LocalTime time;

    @ApiModelProperty(value = "The actual court for the hearing.", position = 4)
    private Agency location;
}
