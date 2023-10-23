package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Schema(description = "Offender sentence and offence details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceAndOffences {
    @Schema(description = "The bookingId this sentence and offence(s) relates to")
    private Long bookingId;

    @Schema(description = "Sentence sequence - a unique identifier a sentence on a booking")
    private Integer sentenceSequence;

    @Schema(description = "This sentence is consecutive to this sequence (if populated)")
    private Integer consecutiveToSequence;

    @Schema(description = "Sentence line sequence - a number representing the order")
    private Long lineSequence;

    @Schema(description = "Case sequence - a number representing the order of the case this sentence belongs to")
    private Integer caseSequence;

    @Schema(description = "Case reference - a string identifying the case this sentence belongs to")
    private String caseReference;

    @Schema(description = "Court description - a string describing the the court that the case was heard at")
    private String courtDescription;

    @Schema(description = "This sentence status: A = Active I = Inactive")
    private String sentenceStatus;

    @Schema(description = "The sentence category e.g. 2003 or Licence")
    private String sentenceCategory;

    @Schema(description = "The sentence calculation type e.g. R or ADIMP_ORA")
    private String sentenceCalculationType;

    @Schema(description = "The sentence type description e.g. Standard Determinate Sentence")
    private String sentenceTypeDescription;

    @Schema(description = "The sentenced date for this sentence (aka court date)")
    private LocalDate sentenceDate;

    @Schema(description = "Date that this sentence would expire")
    private LocalDate sentenceEnd;

    @Schema(description = "The sentence terms of the sentence")
    private List<OffenderSentenceTerm> terms;

    @Schema(description = "The offences related to this sentence (will usually only have one offence per sentence)")
    private List<OffenderOffence> offences;

    @Schema(description = "The amount of fine related to the sentence and offence")
    private Double fineAmount;

    @JsonIgnore
    public boolean isAFine() { return A_FINE_TYPE.equals(sentenceCalculationType); }

    @JsonIgnore
    public boolean isFixedTermRecallType() {
        return FIXED_TERM_RECALL_TYPES.contains(sentenceCalculationType);
    }


    private static final Set<String> FIXED_TERM_RECALL_TYPES = Set.of("14FTR_ORA", "FTR_14_ORA", "FTR", "FTR_ORA", "FTR_SCH15", "FTRSCH15_ORA", "FTRSCH18", "FTRSCH18_ORA");
    private static final String A_FINE_TYPE = "A/FINE";
}
