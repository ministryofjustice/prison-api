package net.syscon.elite.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel(description = "An Adjudication Sanction")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sanction {

    @ApiModelProperty("Sanction Type")
    private String sanctionType;

    @ApiModelProperty("Sanction Days")
    private Long sanctionDays;

    @ApiModelProperty("Sanction Months")
    private Long sanctionMonths;

    @ApiModelProperty("CompensationAmount")
    private Long compensationAmount;

    @ApiModelProperty("Effective")
    private LocalDateTime effectiveDate;

    @ApiModelProperty("Sanction status")
    private String status;

    @ApiModelProperty("Status Date")
    private LocalDateTime statusDate;

    @ApiModelProperty("Comment")
    private String comment;

    @ApiModelProperty("Sanction Seq")
    private Long sanctionSeq;

    @ApiModelProperty("Consecutive Sanction Seq")
    private Long consecutiveSanctionSeq;

    @JsonIgnore
    private long oicHearingId;

    @JsonIgnore
    private Long resultSeq;
}
