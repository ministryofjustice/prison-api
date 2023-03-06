package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Schema(description = "AssessmentDetail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentDetail extends AssessmentSummary {

    @Builder (builderMethodName = "detailBuilder")
    public AssessmentDetail(final AssessmentSummary summary, final String assessmentAgencyId, final String assessmentComment,
                            final String assessmentCommitteeCode, final String assessmentCommitteeName, final LocalDate approvalDate,
                            final String approvalCommitteeCode, final String approvalCommitteeName, final String originalClassificationCode,
                            final String classificationReviewReason, final List<AssessmentQuestion> questions) {
        super(summary.getBookingId(), summary.getAssessmentSeq(), summary.getOffenderNo(), summary.getClassificationCode(),
            summary.getAssessmentCode(), summary.isCellSharingAlertFlag(), summary.getAssessmentDate(), summary.getAssessmentAgencyId(),
            summary.getAssessmentComment(), summary.getAssessorUser(), summary.getNextReviewDate());
        this.assessmentCommitteeCode = assessmentCommitteeCode;
        this.assessmentCommitteeName = assessmentCommitteeName;
        this.approvalDate = approvalDate;
        this.approvalCommitteeCode = approvalCommitteeCode;
        this.approvalCommitteeName = approvalCommitteeName;
        this.originalClassificationCode = originalClassificationCode;
        this.classificationReviewReason = classificationReviewReason ;
        this.questions = questions;
    }

    @Schema(description = "The code of the committee that conducted the assessment", example = "REVIEW")
    private String assessmentCommitteeCode;

    @Schema(description = "The name of the committee that conducted the assessment", example = "REVIEW")
    private String assessmentCommitteeName;

    @Schema(description = "Date of assessment approval", example = "2018-02-11")
    private LocalDate approvalDate;

    @Schema(description = "The code of the committee that conducted the approval", example = "REVIEW")
    private String approvalCommitteeCode;

    @Schema(description = "The name of the committee that conducted the approval", example = "REVIEW")
    private String approvalCommitteeName;

    @Schema(description = "Classification code before it was reviewed", example = "HI")
    private String originalClassificationCode;

    @Schema(description = "The reason for the review of the classification", example = "HI")
    private String classificationReviewReason;

    @NotNull
    @Schema(description = "Assessment questions and answers, in the order they were asked")
    private List<AssessmentQuestion> questions = new ArrayList<>();
}
