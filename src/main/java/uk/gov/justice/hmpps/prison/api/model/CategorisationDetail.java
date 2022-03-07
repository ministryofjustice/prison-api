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
public class CategorisationDetail {

    @ApiModelProperty(required = true, value = "Booking Id", position = 1)
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Category code", position = 2)
    @NotNull(message = "category must be provided")
    private String category;

    @ApiModelProperty(required = true, value = "The assessment committee code (reference code in domain 'ASSESS_COMM')", position = 3)
    @NotNull(message = "committee must be provided")
    private String committee;

    @ApiModelProperty(value = "Next review date for recategorisation, defaults to current date + 6 months, if not provided", position = 4)
    private LocalDate nextReviewDate;

    @ApiModelProperty(value = "Initial categorisation comment", position = 5)
    @Size(max = 4000, message = "Comment text must be a maximum of 4000 characters")
    private String comment;

    @ApiModelProperty(required = true, value = "The prison to be transferred to", position = 6)
    @Size(max = 6, message = "Agency id must be a maximum of 6 characters")
    private String placementAgencyId;

    public LocalDate getNextReviewDate() {
        return nextReviewDate == null ? LocalDate.now().plusMonths(6) : nextReviewDate;
    }
}
