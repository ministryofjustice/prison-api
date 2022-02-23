package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType;

import java.time.LocalDate;

@Schema(description = "Sentence Adjustment values")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SentenceAdjustmentValues {
    @Schema(description = "Sentence sequence", example = "1")
    private Integer sentenceSequence;

    @Schema(description = "Adjustment type")
    private SentenceAdjustmentType type;

    @Schema(description = "Number of days to adjust", example = "12")
    private Integer numberOfDays;

    @Schema(description = "The 'from date' of the adjustment", example = "2022-01-01")
    private LocalDate fromDate;

    @Schema(description = "The 'to date' of the adjustment", example = "2022-01-31")
    private LocalDate toDate;

    @Schema(description = "Boolean flag showing if the adjustment is active", example = "true")
    private boolean active;
}
