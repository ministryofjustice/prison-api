package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Sentence adjustments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentenceAdjustmentDetail {

    @Schema(description = "Number of additional days awarded", example = "12")
    private Integer additionalDaysAwarded;

    @Schema(description = "Number unlawfully at large days", example = "12")
    private Integer unlawfullyAtLarge;

    @Schema(description = "Number of lawfully at large days", example = "12")
    private Integer lawfullyAtLarge;

    @Schema(description = "Number of restored additional days awarded", example = "12")
    private Integer restoredAdditionalDaysAwarded;

    @Schema(description = "Number of special remission days", example = "12")
    private Integer specialRemission;

    @Schema(description = "Number of recall sentence remand days", example = "12")
    private Integer recallSentenceRemand;

    @Schema(description = "Number of recall sentence tagged bail days", example = "12")
    private Integer recallSentenceTaggedBail;

    @Schema(description = "Number of remand days", example = "12")
    private Integer remand;

    @Schema(description = "Number of tagged bail days", example = "12")
    private Integer taggedBail;

    @Schema(description = "Number of unused remand days", example = "12")
    private Integer unusedRemand;
}
