package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@ApiModel(description = "Offender Court Cases")
@Data
@AllArgsConstructor
public class CourtCases {
    @ApiModelProperty(value = "Offender court cases")
    final List<CourtCase> courtCases;
}
