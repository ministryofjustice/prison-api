package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Reasonable Adjustment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
