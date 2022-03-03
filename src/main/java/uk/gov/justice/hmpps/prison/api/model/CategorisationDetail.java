package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Categorisation detail for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorisationDetail {

    @Schema(required = true, description = "Booking Id")
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @Schema(required = true, description = "Category code")
    @NotNull(message = "category must be provided")
    private String category;

    @Schema(required = true, description = "The assessment committee code (reference code in domain 'ASSESS_COMM')")
    @NotNull(message = "committee must be provided")
    private String committee;

    @Schema(description = "Next review date for recategorisation, defaults to current date + 6 months, if not provided")
    private LocalDate nextReviewDate;

    @Schema(description = "Initial categorisation comment")
    @Size(max = 4000, message = "Comment text must be a maximum of 4000 characters")
    private String comment;

    @Schema(required = true, description = "The prison to be transferred to")
    @Size(max = 6, message = "Agency id must be a maximum of 6 characters")
    private String placementAgencyId;

    public LocalDate getNextReviewDate() {
        return nextReviewDate == null ? LocalDate.now().plusMonths(6) : nextReviewDate;
    }
}
