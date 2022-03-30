package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class CategorisationUpdateDetail {

    @Schema(required = true, description = "Booking Id")
    @NotNull(message = "bookingId must be provided")
    private Long bookingId;

    @Schema(required = true, description = "Sequence number")
    @NotNull(message = "Sequence number must be provided")
    private Integer assessmentSeq;

    @Schema(description = "Category code")
    private String category;

    @Schema(description = "The assessment committee code (reference code in domain 'ASSESS_COMM')")
    private String committee;

    @Schema(description = "Next review date for recategorisation")
    private LocalDate nextReviewDate;

    @Schema(description = "Initial categorisation comment")
    @Size(max = 4000, message = "Comment text must be a maximum of 4000 characters")
    private String comment;
}
