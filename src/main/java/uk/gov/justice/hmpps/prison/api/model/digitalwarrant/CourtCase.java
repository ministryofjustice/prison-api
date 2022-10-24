package uk.gov.justice.hmpps.prison.api.model.digitalwarrant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "A new offender court case details entered from a digital warrant.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCase {
    @Schema(description = "The begin date", example = "2019-12-01")
    private LocalDate beginDate;

    @Schema(description = "The location of the court case")
    private String agencyId;

    @Schema(description = "The case type", example = "Adult")
    private String caseType;

    @Schema(description = "The case information number", example = "TD20177010")
    private String caseInfoNumber;

    @Schema(description = "Type of court hearing")
    private String hearingType;

}
