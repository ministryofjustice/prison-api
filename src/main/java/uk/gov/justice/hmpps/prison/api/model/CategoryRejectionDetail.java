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
public class CategoryRejectionDetail {

    @ApiModelProperty(required = true, value = "Booking Id", position = 1)
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Sequence number", position = 2)
    @NotNull(message = "Sequence number must be provided")
    private Integer assessmentSeq;

    @ApiModelProperty(required = true, value = "Date of rejection", position = 3)
    @NotNull(message = "Date of rejection must be provided") // TODO make optional, default today?
    private LocalDate evaluationDate;

    @ApiModelProperty(required = true, value = "Department, reference code in domain 'ASSESS_COMM'. Normally 'REVIEW'", position = 4)
    @NotEmpty(message = "Department must be provided")
    private String reviewCommitteeCode;

    @ApiModelProperty(value = "Overall comment", position = 5)
    @Size(max = 240, message = "Comment text must be a maximum of 240 characters")
    private String committeeCommentText;
}
