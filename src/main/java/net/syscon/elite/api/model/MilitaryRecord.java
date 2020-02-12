package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
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
    private final String warZoneCode;
    private final String warZoneDescription;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String militaryDischargeCode;
    private final String militaryDischargeDescription;
    private final String militaryBranchCode;
    private final String militaryBranchDescription;
    private final String description;
    private final String unitNumber;
    private final String enlistmentLocation;
    private final String dischargeLocation;
    private final boolean selectiveServicesFlag;
    private final String militaryRankCode;
    private final String militaryRankDescription;
    private final String serviceNumber;
    private final String disciplinaryActionCode;
    private final String disciplinaryActionDescription;
}
