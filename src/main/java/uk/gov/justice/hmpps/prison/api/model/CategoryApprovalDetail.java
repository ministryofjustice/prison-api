package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Categorisation approval detail for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryApprovalDetail {

    @Schema(requiredMode = REQUIRED, description = "Booking Id")
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @Schema(description = "Sequence number. Only used to check consistency")
    private Integer assessmentSeq;

    @Schema(requiredMode = REQUIRED, description = "Category code, reference code in domain 'SUP_LVL_TYPE'")
    @NotEmpty(message = "category must be provided")
    private String category;

    @Schema(requiredMode = REQUIRED, description = "Date of approval")
    @NotNull(message = "Date of approval must be provided")
    private LocalDate evaluationDate;

    @Schema(requiredMode = REQUIRED, description = "Department, reference code in domain 'ASSESS_COMM'. Normally 'REVIEW'", example = "REVIEW")
    @NotEmpty(message = "Department must be provided")
    private String reviewCommitteeCode;

    @Schema(description = "Approved result category comment")
    @Size(max = 240, message = "Approved result category comment text must be a maximum of 240 characters")
    private String approvedCategoryComment;

    @Schema(description = "Overall comment")
    @Size(max = 240, message = "Comment text must be a maximum of 240 characters")
    private String committeeCommentText;

    @Schema(description = "Next review date (date of re-assessment, remains unchanged if not provided)")
    private LocalDate nextReviewDate;

    @Schema(description = "Approved placement prison")
    @Size(max = 6, message = "Approved placement prison must be a maximum of 6 characters")
    private String approvedPlacementAgencyId;

    @Schema(description = "Approved placement prison comment")
    @Size(max = 240, message = "Approved placement comment text must be a maximum of 240 characters")
    private String approvedPlacementText;
}
