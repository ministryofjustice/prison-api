package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Offender court case details")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCase {
    @Schema(description = "The case identifier", example = "1")
    private Long id;

    @Schema(description = "The case sequence number for the offender", example = "1")
    private Long caseSeq;

    @Schema(description = "The begin date", example = "2019-12-01")
    private LocalDate beginDate;

    @Schema(description = "Agency details")
    private Agency agency;

    @Schema(description = "The case type", example = "Adult")
    private String caseType;

    @Schema(description = "The prefix of the case number")
    private String caseInfoPrefix;

    @Schema(description = "The case information number", example = "TD20177010")
    private String caseInfoNumber;

    @Schema(description = "The case status", example = "ACTIVE", allowableValues = {"ACTIVE", "CLOSED", "INACTIVE"})
    private String caseStatus;

    @Schema(description = "Court hearings associated with the court case")
    private List<CourtHearing> courtHearings;

    public String getCaseStatus() {
        return StringUtils.isNotBlank(caseStatus) ? caseStatus.toUpperCase() : null;
    }
}
