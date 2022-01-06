package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType;

import java.time.LocalDate;

@ApiModel(description = "Sentence Adjustment values")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SentenceAdjustment {
    @ApiModelProperty(value = "Sentence sequence", example = "1")
    private Integer sentenceSequence;

    @ApiModelProperty(value = "Adjustment type", allowableValues = "RECALL_SENTENCE_REMAND, TAGGED_BAIL, RECALL_SENTENCE_TAGGED_BAIL, REMAND, UNUSED_REMAND")
    private SentenceAdjustmentType type;

    @ApiModelProperty(value = "Number of days to adjust", example = "12")
    private Integer numberOfDays;

    @ApiModelProperty(value = "The 'from date' of the adjustment", example = "2022-01-01")
    private LocalDate fromDate;

    @ApiModelProperty(value = "The 'to date' of the adjustment", example = "2022-01-31")
    private LocalDate toDate;

    @ApiModelProperty(value = "Boolean flag showing if the adjustment is active", example = "true")
    private boolean active;
}
