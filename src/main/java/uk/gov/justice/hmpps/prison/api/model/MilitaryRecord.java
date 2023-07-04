package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Military Record")
@Builder
@AllArgsConstructor
@Data
@JsonInclude(Include.NON_NULL)
public class MilitaryRecord {
    @Schema(description = "War zone code", example = "AFG")
    private final String warZoneCode;
    @Schema(description = "War zone description", example = "Afghanistan")
    private final String warZoneDescription;
    @Schema(description = "Start date", example = "2000-01-01", requiredMode = REQUIRED)
    private final LocalDate startDate;
    @Schema(description = "End date", example = "2020-10-17")
    private final LocalDate endDate;
    @Schema(description = "Military discharge code", example = "DIS")
    private final String militaryDischargeCode;
    @Schema(description = "Military discharge description", example = "Dishonourable")
    private final String militaryDischargeDescription;
    @Schema(description = "Military branch code", example = "ARM", requiredMode = REQUIRED)
    private final String militaryBranchCode;
    @Schema(description = "Military branch description", example = "Army", requiredMode = REQUIRED)
    private final String militaryBranchDescription;
    @Schema(description = "Description", example = "Some description")
    private final String description;
    @Schema(description = "The unit number", example = "255 TACP Battery")
    private final String unitNumber;
    @Schema(description = "Enlistment location", example = "Sheffield")
    private final String enlistmentLocation;
    @Schema(description = "Discharge location", example = "Manchester")
    private final String dischargeLocation;
    @Schema(description = "Selective services flag", example = "false", requiredMode = REQUIRED)
    private final boolean selectiveServicesFlag;
    @Schema(description = "Military rank code", example = "LCPL_RMA")
    private final String militaryRankCode;
    @Schema(description = "Military rank description", example = "Lance Corporal  (Royal Marines)")
    private final String militaryRankDescription;
    @Schema(description = "Service number", example = "25232301")
    private final String serviceNumber;
    @Schema(description = "Disciplinary action code", example = "CM")
    private final String disciplinaryActionCode;
    @Schema(description = "Disciplinary action description", example = "Court Martial")
    private final String disciplinaryActionDescription;
}
