package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Sentence Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SentenceDates {

    @ApiModelProperty(required = true, value = "Sentence sequence - a number representing the order", example = "2")
    private Integer sentenceSequence;

    @ApiModelProperty(required = true, value = "Dates to be updated for this sentence.")
    private OffenderKeyDates offenderKeyDates;
}
