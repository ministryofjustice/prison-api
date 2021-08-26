package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence terms charges for booking id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceCharges {
    @ApiModelProperty(required = true, value = "Offender booking id.", position = 1, example = "1132400")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Sentence number within booking id.", position = 2, example = "2")
    private Integer sentenceSequence;

    @ApiModelProperty(required = true, value = "Offender charge id.", position = 3, example = "1132400")
    private Long offenderChargeId;
}
