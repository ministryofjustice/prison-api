package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Adjustments associated at a booking level and a sentence level")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BookingAndSentenceAdjustments {
    @Schema(description = "Adjustments associated at a sentence level (of type RECALL_SENTENCE_REMAND, TAGGED_BAIL, RECALL_SENTENCE_TAGGED_BAIL, REMAND or UNUSED_REMAND)")
    private List<SentenceAdjustmentValues> sentenceAdjustments;

    @Schema(description = "Adjustments associated at a booking level (of type SPECIAL_REMISSION, ADDITIONAL_DAYS_AWARDED, RESTORED_ADDITIONAL_DAYS_AWARDED, UNLAWFULLY_AT_LARGE, LAWFULLY_AT_LARGE)")
    private List<BookingAdjustment> bookingAdjustments;
}
