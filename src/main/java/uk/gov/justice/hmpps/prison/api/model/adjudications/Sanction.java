package uk.gov.justice.hmpps.prison.api.model.adjudications;

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

    @ApiModelProperty(value = "Sanction Type", example = "Stoppage of Earnings (amount)")
    private String sanctionType;

    @ApiModelProperty(value = "Sanction Days", example = "14")
    private Long sanctionDays;

    @ApiModelProperty(value = "Sanction Months", example = "1")
    private Long sanctionMonths;

    @ApiModelProperty(value = "Compensation Amount", example = "50")
    private Long compensationAmount;

    @ApiModelProperty(value = "Effective", example = "2017-03-22T00:00:00")
    private LocalDateTime effectiveDate;

    @ApiModelProperty(value = "Sanction status", example = "Immediate")
    private String status;

    @ApiModelProperty(value = "Status Date", example = "2017-03-22T00:00:00")
    private LocalDateTime statusDate;

    @ApiModelProperty(value = "Comment", example = "14x LOTV, 14x LOGYM, 14x LOC, 14x LOA, 14x LOE 50%, 14x CC")
    private String comment;

    @ApiModelProperty(value = "Sanction Seq", example = "1")
    private Long sanctionSeq;

    @ApiModelProperty(value = "Consecutive Sanction Seq", example = "1")
    private Long consecutiveSanctionSeq;

    @JsonIgnore
    private long oicHearingId;

    @JsonIgnore
    private Long resultSeq;
}
