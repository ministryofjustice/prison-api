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
public class CategoryRejectionDetail {

    @Schema(requiredMode = REQUIRED, description = "Booking Id")
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Sequence number")
    @NotNull(message = "Sequence number must be provided")
    private Integer assessmentSeq;

    @Schema(requiredMode = REQUIRED, description = "Date of rejection")
    @NotNull(message = "Date of rejection must be provided") // TODO make optional, default today?
    private LocalDate evaluationDate;

    @Schema(requiredMode = REQUIRED, description = "Department, reference code in domain 'ASSESS_COMM'. Normally 'REVIEW'")
    @NotEmpty(message = "Department must be provided")
    private String reviewCommitteeCode;

    @Schema(description = "Overall comment")
    @Size(max = 240, message = "Comment text must be a maximum of 240 characters")
    private String committeeCommentText;
}
