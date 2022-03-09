package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@ApiModel(description = "Military Record")
@Builder
@AllArgsConstructor
@Data
@JsonInclude(Include.NON_NULL)
public class MilitaryRecord {
    @ApiModelProperty(value = "War zone code", position = 1, example = "AFG")
    private final String warZoneCode;
    @ApiModelProperty(value = "War zone description", position = 2, example = "Afghanistan")
    private final String warZoneDescription;
    @ApiModelProperty(value = "Start date", position = 3, example = "2000-01-01", required = true)
    private final LocalDate startDate;
    @ApiModelProperty(value = "End date", position = 4, example = "2020-10-17")
    private final LocalDate endDate;
    @ApiModelProperty(value = "Military discharge code", position = 5, example = "DIS")
    private final String militaryDischargeCode;
    @ApiModelProperty(value = "Military discharge description", position = 6, example = "Dishonourable")
    private final String militaryDischargeDescription;
    @ApiModelProperty(value = "Military branch code", position = 7, example = "ARM", required = true)
    private final String militaryBranchCode;
    @ApiModelProperty(value = "Military branch description", position = 8, example = "Army", required = true)
    private final String militaryBranchDescription;
    @ApiModelProperty(value = "Description", position = 9, example = "Some description")
    private final String description;
    @ApiModelProperty(value = "The unit number", position = 10, example = "255 TACP Battery")
    private final String unitNumber;
    @ApiModelProperty(value = "Enlistment location", position = 11, example = "Sheffield")
    private final String enlistmentLocation;
    @ApiModelProperty(value = "Discharge location", position = 12, example = "Manchester")
    private final String dischargeLocation;
    @ApiModelProperty(value = "Selective services flag", position = 13, example = "false", required = true)
    private final boolean selectiveServicesFlag;
    @ApiModelProperty(value = "Military rank code", position = 14, example = "LCPL_RMA")
    private final String militaryRankCode;
    @ApiModelProperty(value = "Military rank description", position = 15, example = "Lance Corporal  (Royal Marines)")
    private final String militaryRankDescription;
    @ApiModelProperty(value = "Service number", position = 16, example = "25232301")
    private final String serviceNumber;
    @ApiModelProperty(value = "Disciplinary action code", position = 17, example = "CM")
    private final String disciplinaryActionCode;
    @ApiModelProperty(value = "Disciplinary action description", position = 18, example = "Court Martial")
    private final String disciplinaryActionDescription;
}
