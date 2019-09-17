package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;

@ApiModel(description = "Personal Care Needs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class PersonalCareNeed {

    @ApiModelProperty(value = "Problem Type", position = 1, example = "MATSTAT")
    private String problemType;

    @ApiModelProperty(value = "Problem Code", position = 2, example = "ACCU9")
    private String problemCode;

    @ApiModelProperty(value = "Problem Status", position = 3, example = "ON")
    private String problemStatus;

    @ApiModelProperty(value = "Problem Description", position = 4, example = "Preg, acc under 9mths")
    private String problemDescription;

    @ApiModelProperty(value = "Start Date", position = 5, example = "2010-06-21")
    private LocalDate startDate;

    @ApiModelProperty(value = "End Date", position = 6, example = "2010-06-21")
    private LocalDate endDate;
}
