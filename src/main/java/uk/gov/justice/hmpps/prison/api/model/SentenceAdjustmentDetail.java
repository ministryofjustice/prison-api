package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Sentence adjustments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentenceAdjustmentDetail {

    @ApiModelProperty(value = "Number of additional days awarded", example = "12")
    private Integer additionalDaysAwarded;

    @ApiModelProperty(value = "Number unlawfully at large days", example = "12")
    private Integer unlawfullyAtLarge;

    @ApiModelProperty(value = "Number of lawfully at large days", example = "12")
    private Integer lawfullyAtLarge;

    @ApiModelProperty(value = "Number of restored additional days awarded", example = "12")
    private Integer restoredAdditionalDaysAwarded;

    @ApiModelProperty(value = "Number of special remission days", example = "12")
    private Integer specialRemission;

    @ApiModelProperty(value = "Number of recall sentence remand days", example = "12")
    private Integer recallSentenceRemand;

    @ApiModelProperty(value = "Number of recall sentence tagged bail days", example = "12")
    private Integer recallSentenceTaggedBail;

    @ApiModelProperty(value = "Number of remand days", example = "12")
    private Integer remand;

    @ApiModelProperty(value = "Number of tagged bail days", example = "12")
    private Integer taggedBail;

    @ApiModelProperty(value = "Number of unused remand days", example = "12")
    private Integer unusedRemand;
}
