package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("unused")
@ApiModel(description = "Offender sentence and offence details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class OffenderSentenceAndOffences {
    @ApiModelProperty(value = "bookingId")
    private Long bookingId;

    @ApiModelProperty(value = "Sentence sequence")
    private Integer sentenceSequence;

    @ApiModelProperty(value = "Consecutive to sequence")
    private Integer consecutiveToSequence;

    // TODO add swagger annotations
    private String sentenceStatus;
    private String sentenceCategory;
    private String sentenceCalculationType;
    private String sentenceTypeDescription;
    private LocalDate sentenceDate;
    private Integer years;
    private Integer months;
    private Integer weeks;
    private Integer days;

    private List<OffenderOffence> offences;
}
