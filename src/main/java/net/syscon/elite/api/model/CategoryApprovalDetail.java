package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Categorisation approval detail for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryApprovalDetail {

    @ApiModelProperty(required = true, value = "Booking Id")
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Category code, reference code in domain 'SUP_LVL_TYPE'")
    @NotNull(message = "category must be provided")
    private String category;

    @NotNull(message = "Date of approval must be provided")
    @ApiModelProperty(value = "Date of approval")
    private LocalDate evaluationDate;

    @ApiModelProperty(value = "Approved result comment")
    private String  reviewSupLevelText;

//    @NotNull(message = "Department must be provided")
//    @ApiModelProperty(value = "Department, reference code in domain 'ASSESS_COMM'. Normally 'REVIEW'")
//    private String   reviewCommitteeCode;
//
//    @ApiModelProperty(value = "Overall comment")
//    private String  committeeCommentText;
//
//    @ApiModelProperty(value = "Approved placement comment")
//    private String   reviewPlacementText;
//
//    @ApiModelProperty(value = "Next review date (date of recategorisation)")
//    private LocalDate nextReviewDate;
}
