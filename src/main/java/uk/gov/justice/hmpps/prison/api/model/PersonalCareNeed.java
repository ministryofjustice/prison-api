package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Personal Care Need")
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

    @ApiModelProperty(value = "Comment Text", position = 5, example = "a comment")
    private String commentText;

    @ApiModelProperty(value = "Start Date", position = 6, example = "2010-06-21")
    private LocalDate startDate;

    @ApiModelProperty(value = "End Date", position = 7, example = "2010-06-21")
    private LocalDate endDate;

    @JsonIgnore
    private String offenderNo;
}
