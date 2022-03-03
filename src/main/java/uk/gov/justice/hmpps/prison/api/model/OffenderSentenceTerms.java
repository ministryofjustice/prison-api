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
@ApiModel(description = "Offender Sentence terms details for booking id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceTerms {
    @ApiModelProperty(required = true, value = "Offender booking id.", position = 1, example = "1132400")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Sentence number within booking id.", position = 2, example = "2")
    private Integer sentenceSequence;

    @ApiModelProperty(required = true, value = "Sentence term number within sentence.", position = 3, example = "1")
    private Integer termSequence;

    @ApiModelProperty(value = "Sentence number which this sentence follows if consecutive, otherwise concurrent.", position = 4, example = "2")
    private Integer consecutiveTo;

    @ApiModelProperty(value = "Sentence type, using reference data from table SENTENCE_CALC_TYPES.", position = 5, example = "2")
    private String sentenceType;

    @ApiModelProperty(value = "Sentence type description.", position = 6, example = "2")
    private String sentenceTypeDescription;

    @ApiModelProperty(required = true, value = "Start date of sentence term.", position = 7, example = "2018-12-31")
    private LocalDate startDate;

    @ApiModelProperty(value = "Sentence length years.", position = 8)
    private Integer years;

    @ApiModelProperty(value = "Sentence length months.", position = 9)
    private Integer months;

    @ApiModelProperty(value = "Sentence length weeks.", position = 10)
    private Integer weeks;

    @ApiModelProperty(value = "Sentence length days.", position = 11)
    private Integer days;

    @ApiModelProperty(required = true, value = "Whether this is a life sentence.", position = 12)
    private Boolean lifeSentence;

    @ApiModelProperty(required = true, value = "Court case id", position = 13)
    private String caseId;

    @ApiModelProperty(required = true, value = "Fine amount.", position = 14)
    private Double fineAmount;

    @ApiModelProperty(required = true, value = "Sentence term code.", position = 15, example = "IMP")
    private String sentenceTermCode;

    @ApiModelProperty(required = true, value = "Sentence line number", position = 16, example = "1")
    private Long lineSeq;

    @ApiModelProperty(required = true, value = "Sentence start date", position = 17, example = "2018-12-31")
    private LocalDate sentenceStartDate;
}
