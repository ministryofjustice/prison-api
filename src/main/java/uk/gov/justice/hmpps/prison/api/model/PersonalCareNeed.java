package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "Personal Care Need")
@Data
@Builder
@Getter
@EqualsAndHashCode
public class PersonalCareNeed {

    @Schema(description = "Problem Type", example = "MATSTAT")
    private String problemType;

    @Schema(description = "Problem Code", example = "ACCU9")
    private String problemCode;

    @Schema(description = "Problem Status", example = "ON")
    private String problemStatus;

    @Schema(description = "Problem Description", example = "Preg, acc under 9mths")
    private String problemDescription;

    @Schema(description = "Comment Text", example = "a comment")
    private String commentText;

    @Schema(description = "Start Date", example = "2010-06-21")
    private LocalDate startDate;

    @Schema(description = "End Date", example = "2010-06-21")
    private LocalDate endDate;

    @JsonIgnore
    private String offenderNo;

    public PersonalCareNeed(String problemType, String problemCode, String problemStatus, String problemDescription, String commentText, LocalDate startDate, LocalDate endDate, String offenderNo) {
        this.problemType = problemType;
        this.problemCode = problemCode;
        this.problemStatus = problemStatus;
        this.problemDescription = problemDescription;
        this.commentText = commentText;
        this.startDate = startDate;
        this.endDate = endDate;
        this.offenderNo = offenderNo;
    }

    public PersonalCareNeed() {
    }
}
