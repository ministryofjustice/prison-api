package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Schema(description = "Reasonable Adjustment")
@Data
@Builder
@EqualsAndHashCode
public class ReasonableAdjustment {

    @Schema(description = "Treatment Code", example = "WHEELCHR_ACC")
    private String treatmentCode;

    @Schema(description = "Comment Text", example = "abcd")
    private String commentText;

    @Schema(description = "Start Date", example = "2010-06-21")
    private LocalDate startDate;

    @Schema(description = "End Date", example = "2010-06-21")
    private LocalDate endDate;

    @Schema(description = "The agency id where the adjustment was created", example = "LEI")
    private String agencyId;

    @Schema(description = "Treatment Description", example = "Wheelchair accessibility")
    private String treatmentDescription;

    public ReasonableAdjustment(String treatmentCode, String commentText, LocalDate startDate, LocalDate endDate, String agencyId, String treatmentDescription) {
        this.treatmentCode = treatmentCode;
        this.commentText = commentText;
        this.startDate = startDate;
        this.endDate = endDate;
        this.agencyId = agencyId;
        this.treatmentDescription = treatmentDescription;
    }

    public ReasonableAdjustment() {
    }
}
