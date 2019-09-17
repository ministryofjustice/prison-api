package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;

@ApiModel(description = "Reasonable Adjustment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ReasonableAdjustment {

    @ApiModelProperty(value = "Treatment Code", position = 1, example = "WHEELCHR_ACC")
    private String treatmentCode;

    @ApiModelProperty(value = "Comment Text", position = 2, example = "abcd")
    private String commentText;

    @ApiModelProperty(value = "Description", position = 3, example = "ABCD")
    private String description;

    @ApiModelProperty(value = "Start Date", position = 4, example = "2010-06-21")
    private LocalDate startDate;

    @ApiModelProperty(value = "End Date", position = 5, example = "2010-06-21")
    private LocalDate endDate;
}
