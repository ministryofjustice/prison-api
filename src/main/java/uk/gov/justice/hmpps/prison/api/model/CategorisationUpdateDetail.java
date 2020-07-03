package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@ApiModel(description = "Categorisation detail for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorisationUpdateDetail {

    @ApiModelProperty(required = true, value = "Booking Id", position = 1)
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Sequence number", position = 2)
    @NotNull(message = "Sequence number must be provided")
    private Integer assessmentSeq;

    @ApiModelProperty(value = "Category code", position = 3)
    private String category;

    @ApiModelProperty(value = "The assessment committee code (reference code in domain 'ASSESS_COMM')", position = 4)
    private String committee;

    @ApiModelProperty(value = "Next review date for recategorisation", position = 5)
    private LocalDate nextReviewDate;

    @ApiModelProperty(value = "Initial categorisation comment", position = 6)
    @Size(max = 4000, message = "Comment text must be a maximum of 4000 characters")
    private String comment;
}
