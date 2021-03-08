package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
@Builder
public class AssessmentDetail {

    @NotNull
    @ApiModelProperty(value = "Booking number", position = 1, example = "123456")
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "Sequence number of assessment within booking", position = 2, example = "1")
    private Integer assessmentSeq;

    @NotBlank
    @ApiModelProperty(value = "Offender number (e.g. NOMS Number).", position = 3, example = "GV09876N")
    private String offenderNo;

    @ApiModelProperty(value = "Classification code", position = 4, example = "STANDARD")
    private String classificationCode;

    @NotBlank
    @ApiModelProperty(value = "Identifies the type of assessment", position = 5, example = "CSR")
    private String assessmentCode;

    @NotNull
    @ApiModelProperty(value = "Indicates whether this is a CSRA assessment", position = 6)
    private boolean cellSharingAlertFlag;

    @NotNull
    @ApiModelProperty(value = "Date assessment was created", position = 7, example = "2018-02-11")
    private LocalDate assessmentDate;

    @ApiModelProperty(value = "The assessment creation agency id", position = 8, example = "MDI")
    private String assessmentAgencyId;

    @ApiModelProperty(value = "Comment from assessor", position = 9, example = "Comment details")
    private String assessmentComment;

    @ApiModelProperty(value = "The code of the committee that conducted the assessment", position = 10, example = "REVIEW")
    private String assessmentCommitteeCode;

    @ApiModelProperty(value = "The name of the committee that conducted the assessment", position = 11, example = "REVIEW")
    private String assessmentCommitteeName;

    @ApiModelProperty(value = "Username who made the assessment", position = 12, example = "NGK33Y")
    private String assessorUser;

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

    @ApiModelProperty(value = "Date of next review", position = 18, example = "2018-02-11")
    private LocalDate nextReviewDate;

    @NotNull
    @Builder.Default
    @ApiModelProperty(value = "Assessment questions and answers, in the order they were asked", position = 31)
    private List<AssessmentQuestion> questions = new ArrayList<>();
}
