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

    @Builder(builderMethodName = "detailBuilder")
    public AssessmentDetail(final AssessmentSummary summary, final String assessmentAgencyId, final String assessmentComment,
                            final String assessmentCommitteeCode, final String assessmentCommitteeName, final LocalDate approvalDate,
                            final String approvalCommitteeCode, final String approvalCommitteeName, final String originalClassificationCode,
                            final String classificationReviewReason, final List<AssessmentQuestion> questions, final String overridingClassificationCode,
                            final String calculatedClassificationCode, final String approvedClassificationCode, final String approvalComment,
                            final String overrideReason) {
        super(summary.getBookingId(), summary.getAssessmentSeq(), summary.getOffenderNo(), summary.getClassificationCode(),
            summary.getAssessmentCode(), summary.isCellSharingAlertFlag(), summary.getAssessmentDate(), summary.getAssessmentAgencyId(),
            summary.getAssessmentComment(), summary.getAssessorUser(), summary.getNextReviewDate());
        this.assessmentCommitteeCode = assessmentCommitteeCode;
        this.assessmentCommitteeName = assessmentCommitteeName;
        this.approvalDate = approvalDate;
        this.approvalCommitteeCode = approvalCommitteeCode;
        this.approvalCommitteeName = approvalCommitteeName;
        this.originalClassificationCode = originalClassificationCode;
        this.classificationReviewReason = classificationReviewReason;
        this.questions = questions;
        this.overridingClassificationCode = overridingClassificationCode;
        this.calculatedClassificationCode = calculatedClassificationCode;
        this.approvedClassificationCode = approvedClassificationCode;
        this.approvalComment = approvalComment;
        this.overrideReason = overrideReason;
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

    @Schema(description = "The classification code entered to override the calculated value prior to approval", example = "HI")
    private String overridingClassificationCode;

    @Schema(description = "The classification code originally calculated by NOMIS based on the answers given to the questions when carrying out the initial review", example = "HI")
    private String calculatedClassificationCode;

    @Schema(description = "The classification code that has been approved", example = "HI")
    private String approvedClassificationCode;

    @Schema(description = "Comment added at approval of classification code", example = "Comment")
    private String approvalComment;

    @Schema(description = "The reason given for overriding the calculated classification code", example = "Overriding comment")
    private String overrideReason;

    @NotNull
    @Schema(description = "Assessment questions and answers, in the order they were asked")
    private List<AssessmentQuestion> questions = new ArrayList<>();
}
