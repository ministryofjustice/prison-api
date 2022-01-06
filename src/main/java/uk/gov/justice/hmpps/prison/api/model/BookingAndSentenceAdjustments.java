package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Adjustments associated at a booking level and a sentence level")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BookingAndSentenceAdjustments {
    @ApiModelProperty(value = "Adjustments associated at a sentence level (of type RECALL_SENTENCE_REMAND, TAGGED_BAIL, RECALL_SENTENCE_TAGGED_BAIL, REMAND or UNUSED_REMAND)")
    private List<SentenceAdjustment> sentenceAdjustments;

    @ApiModelProperty(value = "Adjustments associated at a booking level (of type SPECIAL_REMISSION, ADDITIONAL_DAYS_AWARDED, RESTORED_ADDITIONAL_DAYS_AWARDED, UNLAWFULLY_AT_LARGE, LAWFULLY_AT_LARGE)")
    private List<BookingAdjustment> bookingAdjustments;
}
