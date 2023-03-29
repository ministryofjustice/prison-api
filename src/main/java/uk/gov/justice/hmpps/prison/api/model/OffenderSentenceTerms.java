package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@SuppressWarnings("unused")
@Schema(description = "Offender Sentence terms details for booking id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceTerms {
    @Schema(description = "Offender booking id.", example = "1132400")
    private Long bookingId;

    @Schema(description = "Sentence number within booking id.", example = "2")
    private Integer sentenceSequence;

    @Schema(description = "Sentence term number within sentence.", example = "1")
    private Integer termSequence;

    @Schema(description = "Sentence number which this sentence follows if consecutive, otherwise concurrent.", example = "2")
    private Integer consecutiveTo;

    @Schema(description = "Sentence type, using reference data from table SENTENCE_CALC_TYPES.", example = "2")
    private String sentenceType;

    @Schema(description = "Sentence type description.", example = "2")
    private String sentenceTypeDescription;

    @Schema(description = "Start date of sentence term.", example = "2018-12-31")
    private LocalDate startDate;

    @Schema(description = "Sentence length years.")
    private Integer years;

    @Schema(description = "Sentence length months.")
    private Integer months;

    @Schema(description = "Sentence length weeks.")
    private Integer weeks;

    @Schema(description = "Sentence length days.")
    private Integer days;

    @Schema(description = "Whether this is a life sentence.")
    private Boolean lifeSentence;

    @Schema(description = "Court case id")
    private String caseId;

    @Schema(description = "Fine amount.")
    private Double fineAmount;

    @Schema(description = "Sentence term code.", example = "IMP")
    private String sentenceTermCode;

    @Schema(description = "Sentence line number", example = "1")
    private Long lineSeq;

    @Schema(description = "Sentence start date", example = "2018-12-31")
    private LocalDate sentenceStartDate;
}
