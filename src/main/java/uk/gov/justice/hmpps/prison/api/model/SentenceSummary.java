package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApiModel(description = "Sentence Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SentenceSummary {

    @ApiModelProperty(value = "Prisoner Identifier", example = "A1234AA", required = true)
    private String prisonerNumber;

    @ApiModelProperty(value = "Most recent term in prison")
    private PrisonTerm latestPrisonTerm;

    @ApiModelProperty(value = "Other prison terms")
    private List<PrisonTerm> previousPrisonTerms;


    @ApiModel(description = "Prison Term")
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PrisonTerm {
        @ApiModelProperty(value = "Book Number (Prison) / Prison Number (Probation)", example = "B45232", required = true)
        private String bookNumber;

        @ApiModelProperty(value = "Booking Identifier (internal)", example = "12312312", required = true)
        private Long bookingId;

        private List<CourtSentences> courtSentences;

        @ApiModelProperty(value = "Licence sentences")
        private List<SentencesOffencesTerms> licenceSentences;

        private SentenceCalcDates keyDates;

        private SentenceAdjustmentDetail sentenceAdjustments;

        private String effectiveSentenceLength;

        public static PrisonTerm transform(final OffenderBooking booking) {
            return PrisonTerm.builder()
                .bookNumber(booking.getBookNumber())
                .bookingId(booking.getBookingId())
                .keyDates(booking.getSentenceCalcDates())
                .sentenceAdjustments(booking.getSentenceAdjustmentDetail())
                .effectiveSentenceLength(booking.getLatestCalculation().map(SentenceCalculation::getEffectiveSentenceLength).orElse(null))
                .courtSentences(booking.getCourtOrders().stream()
                    .filter(order -> order.getCourtCase() != null && !order.getSentences().isEmpty())
                    .sorted(Comparator.comparing(order -> order.getCourtCase().getCaseSeq()))
                    .map(CourtSentences::transform).toList())
                .licenceSentences(booking.getLicenceSentences().stream().map(SentencesOffencesTerms::transform).toList())
                .build();
        }
    }

    @ApiModel(description = "Court case details")
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourtSentences {
        @ApiModelProperty(value = "The case information number", example = "TD20177010")
        private String caseInfoNumber;

        @ApiModelProperty(value = "The case identifier (internal)", example = "1")
        private Long id;

        @ApiModelProperty(value = "The case sequence number for the offender", example = "1")
        private Long caseSeq;

        @ApiModelProperty(value = "The begin date of the court hearings", example = "2019-12-01")
        private LocalDate beginDate;

        @ApiModelProperty(value = "Court details")
        private Agency court;

        @ApiModelProperty(value = "The case type", example = "Adult")
        private String caseType;

        @ApiModelProperty(value = "The prefix of the case number")
        private String caseInfoPrefix;

        @ApiModelProperty(value = "The case status", example = "ACTIVE", allowableValues = "ACTIVE, CLOSED, INACTIVE")
        private String caseStatus;

        @ApiModelProperty(value = "Court sentences associated with the court case")
        private List<SentencesOffencesTerms> sentences;

        @ApiModelProperty(value = "Issuing Court Details")
        private Agency issuingCourt;

        @ApiModelProperty(value = "Issuing Court Date")
        private LocalDate issuingCourtDate;

        public String getCaseStatus() {
            return StringUtils.isNotBlank(caseStatus) ? caseStatus.toUpperCase() : null;
        }

        public static CourtSentences transform(final CourtOrder order) {
            return CourtSentences.builder()
                .caseInfoNumber(order.getCourtCase().getCaseInfoNumber())
                .id(order.getCourtCase().getId())
                .caseSeq(order.getCourtCase().getCaseSeq())
                .beginDate(order.getCourtCase().getBeginDate())
                .court(AgencyTransformer.transform(order.getCourtCase().getAgencyLocation(), false))
                .caseType(order.getCourtCase().getLegalCaseType().map(LegalCaseType::getDescription).orElse(null))
                .caseInfoPrefix(order.getCourtCase().getCaseInfoPrefix())
                .caseStatus(order.getCourtCase().getCaseStatus().map(CaseStatus::getDescription).orElse(null))
                .sentences(order.getSentences().stream()
                    .sorted(Comparator.comparing(OffenderSentence::getSequence))
                    .map(SentencesOffencesTerms::transform).toList())
                .issuingCourt(AgencyTransformer.transform(order.getIssuingCourt(), false))
                .issuingCourtDate(order.getCourtDate())
                .build();
        }

    }

    @ApiModel(description = "Offender sentence and offence details")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SentencesOffencesTerms {
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

        @ApiModelProperty(value = "The sentence start date for this sentence (aka court date)")
        private LocalDate sentenceStartDate;

        @ApiModelProperty(value = "The sentence end date for this sentence")
        private LocalDate sentenceEndDate;

        @ApiModelProperty(required = true, value = "Fine amount.", position = 14)
        private Double fineAmount;

        @ApiModelProperty(required = true, value = "Sentence line number", position = 16, example = "1")
        private Long lineSeq;

        @ApiModelProperty(value = "The offences related to this sentence (will usually only have one offence per sentence)")
        private List<OffenderOffence> offences;

        @ApiModelProperty(value = "The terms related to this sentence (will usually only have one term per sentence)")
        private List<Terms> terms;

        public static SentencesOffencesTerms transform(final OffenderSentence sentence) {
            return SentencesOffencesTerms.builder()
                .sentenceSequence(sentence.getSequence())
                .consecutiveToSequence(sentence.getConsecutiveToSentenceSequence())
                .sentenceStatus(sentence.getStatus())
                .sentenceCategory(sentence.getCalculationType().getCategory())
                .sentenceCalculationType(sentence.getCalculationType().getCalculationType())
                .sentenceTypeDescription(sentence.getCalculationType().getDescription())
                .sentenceStartDate(sentence.getSentenceStartDate())
                .sentenceEndDate(sentence.getSentenceEndDate())
                .lineSeq(sentence.getLineSequence())
                .terms(sentence.getTerms().stream().map(Terms::transform).toList())
                .offences(sentence.getOffenderSentenceCharges() == null ? null : sentence.getOffenderSentenceCharges()
                    .stream()
                    .map(OffenderSentenceCharge::getOffenderCharge)
                    .map(OffenderCharge::getOffenceDetail)
                    .collect(Collectors.toList()))
                .build();
        }

    }

    @ApiModel(description = "Offender Sentence terms details for booking id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Terms {

        @ApiModelProperty(required = true, value = "Sentence term number within sentence.", position = 3, example = "1")
        private Integer termSequence;

        @ApiModelProperty(value = "Sentence number which this sentence follows if consecutive, otherwise concurrent.", position = 4, example = "2")
        private Integer consecutiveTo;

        @ApiModelProperty(value = "Sentence type, using reference data from table SENTENCE_CALC_TYPES.", position = 5, example = "2")
        private String sentenceType;

        @ApiModelProperty(required = true, value = "Sentence term code.", position = 15, example = "IMP")
        private String sentenceTermCode;

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


        public static Terms transform(final SentenceTerm terms) {
            return Terms.builder()
                .termSequence(terms.getTermSequence())
                .sentenceTermCode(terms.getSentenceTermCode())
                .lifeSentence("Y".equals(terms.getLifeSentenceFlag()))
                .days(terms.getDays())
                .weeks(terms.getWeeks())
                .months(terms.getMonths())
                .years(terms.getYears())
                .startDate(terms.getStartDate())
                .build();
        }
    }
}
