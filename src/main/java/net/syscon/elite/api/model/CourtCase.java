package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@ApiModel(description = "Offender court case details")
@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCase {
    @ApiModelProperty(value = "The case identifier", position = 1, example = "1")
    private final Long id;

    @ApiModelProperty(value = "The case sequence number for the offender", position = 2, example = "1")
    private Long caseSeq;

    @ApiModelProperty(value = "The begin date", position = 3, example = "2019-12-01")
    private LocalDate beginDate;

    @ApiModelProperty(value = "Agency details", position = 4)
    private Agency agency;

    @ApiModelProperty(value = "The case type", position = 5, example = "Adult")
    private String caseType;

    @ApiModelProperty(value = "The prefix of the case number", position = 6)
    private String caseInfoPrefix;

    @ApiModelProperty(value = "The case information number", position = 7, example = "TD20177010")
    private String caseInfoNumber;

    @ApiModelProperty(value = "The case status", position = 8, example = "Active")
    private String caseStatus;
}
