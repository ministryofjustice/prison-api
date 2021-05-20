package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@ApiModel(description = "AssessmentDetail")
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

    @ApiModelProperty(value = "The code of the committee that conducted the assessment", position = 10, example = "REVIEW")
    private String assessmentCommitteeCode;

    @ApiModelProperty(value = "The name of the committee that conducted the assessment", position = 11, example = "REVIEW")
    private String assessmentCommitteeName;

    @ApiModelProperty(value = "Date of assessment approval", position = 13, example = "2018-02-11")
    private LocalDate approvalDate;

    @ApiModelProperty(value = "The code of the committee that conducted the approval", position = 14, example = "REVIEW")
    private String approvalCommitteeCode;

    @ApiModelProperty(value = "The name of the committee that conducted the approval", position = 15, example = "REVIEW")
    private String approvalCommitteeName;

    @ApiModelProperty(value = "Classification code before it was reviewed", position = 16, example = "HI")
    private String originalClassificationCode;

    @ApiModelProperty(value = "The reason for the review of the classification", position = 17, example = "HI")
    private String classificationReviewReason;

    @NotNull
    @ApiModelProperty(value = "Assessment questions and answers, in the order they were asked", position = 31)
    private List<AssessmentQuestion> questions = new ArrayList<>();
}
