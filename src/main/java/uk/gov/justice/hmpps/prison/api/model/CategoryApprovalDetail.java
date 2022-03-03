package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@ApiModel(description = "Categorisation approval detail for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryApprovalDetail {

    @ApiModelProperty(required = true, value = "Booking Id", position = 1)
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @ApiModelProperty(value = "Sequence number. Only used to check consistency", position = 2)
    private Integer assessmentSeq;

    @ApiModelProperty(required = true, value = "Category code, reference code in domain 'SUP_LVL_TYPE'", position = 3)
    @NotEmpty(message = "category must be provided")
    private String category;

    @ApiModelProperty(required = true, value = "Date of approval", position = 4)
    @NotNull(message = "Date of approval must be provided")
    private LocalDate evaluationDate;

    @ApiModelProperty(required = true, value = "Department, reference code in domain 'ASSESS_COMM'. Normally 'REVIEW'", example = "REVIEW", position = 5)
    @NotEmpty(message = "Department must be provided")
    private String reviewCommitteeCode;

    @ApiModelProperty(value = "Approved result category comment", position = 6)
    @Size(max = 240, message = "Approved result category comment text must be a maximum of 240 characters")
    private String approvedCategoryComment;

    @ApiModelProperty(value = "Overall comment", position = 7)
    @Size(max = 240, message = "Comment text must be a maximum of 240 characters")
    private String committeeCommentText;

    @ApiModelProperty(value = "Next review date (date of re-assessment, remains unchanged if not provided)", position = 8)
    private LocalDate nextReviewDate;

    @ApiModelProperty(value = "Approved placement prison", position = 9)
    @Size(max = 6, message = "Approved placement prison must be a maximum of 6 characters")
    private String approvedPlacementAgencyId;

    @ApiModelProperty(value = "Approved placement prison comment", position = 10)
    @Size(max = 240, message = "Approved placement comment text must be a maximum of 240 characters")
    private String approvedPlacementText;
}
