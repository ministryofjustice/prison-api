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

@ApiModel(description = "Offender sentence and offence details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceAndOffences {
    @ApiModelProperty(value = "The bookingId this sentence and offence(s) relates to")
    private Long bookingId;

    @ApiModelProperty(value = "Sentence sequence - a number representing the order")
    private Integer sentenceSequence;

    @ApiModelProperty(value = "This sentence is consecutive to this sequence (if populated)")
    private Integer consecutiveToSequence;

    @ApiModelProperty(value = "This sentence status: A = Active I = Inactive")
    private String sentenceStatus;

    @ApiModelProperty(value = "The sentence category e.g. 2003 or Licence")
    private String sentenceCategory;

    @ApiModelProperty(value = "The sentence calculation type e.g. R or ADIMP_ORA")
    private String sentenceCalculationType;

    @ApiModelProperty(value = "The sentence type description e.g. Standard Determinate Sentence")
    private String sentenceTypeDescription;

    @ApiModelProperty(value = "The sentenced date for this sentence (aka court date)")
    private LocalDate sentenceDate;

    @ApiModelProperty(value = "The sentence duration im years")
    private Integer years;

    @ApiModelProperty(value = "The sentence duration im months")
    private Integer months;

    @ApiModelProperty(value = "The sentence duration im weeks")
    private Integer weeks;

    @ApiModelProperty(value = "The sentence duration in days")
    private Integer days;

    @ApiModelProperty(value = "The offences related to this sentence (will usually only have one offence per sentence)")
    private List<OffenderOffence> offences;
}
