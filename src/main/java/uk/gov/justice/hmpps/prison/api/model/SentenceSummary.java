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
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.NonDtoReleaseDateType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

        private KeyDates keyDates;

        private SentenceAdjustmentDetail sentenceAdjustments;

        public static PrisonTerm transform(final OffenderBooking booking) {
            return PrisonTerm.builder()
                .bookNumber(booking.getBookNumber())
                .bookingId(booking.getBookingId())
                .keyDates(KeyDates.transform(booking, booking.getLatestCalculation()))
                .sentenceAdjustments(booking.getSentenceAdjustmentDetail())
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

        @ApiModelProperty(required = true, value = "Fine amount.")
        private Double fineAmount;

        @ApiModelProperty(required = true, value = "Sentence line number", example = "1")
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
                .fineAmount(sentence.getFineAmount())
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

        @ApiModelProperty(required = true, value = "Sentence term number within sentence.", example = "1")
        private Integer termSequence;

        @ApiModelProperty(value = "Sentence number which this sentence follows if consecutive, otherwise concurrent.", example = "2")
        private Integer consecutiveTo;

        @ApiModelProperty(value = "Sentence type, using reference data from table SENTENCE_CALC_TYPES.", example = "2")
        private String sentenceType;

        @ApiModelProperty(required = true, value = "Sentence term code.", example = "IMP")
        private String sentenceTermCode;

        @ApiModelProperty(value = "Sentence type description.", example = "2")
        private String sentenceTypeDescription;

        @ApiModelProperty(required = true, value = "Start date of sentence term.", example = "2018-12-31")
        private LocalDate startDate;

        @ApiModelProperty(value = "Sentence length years.")
        private Integer years;

        @ApiModelProperty(value = "Sentence length months.")
        private Integer months;

        @ApiModelProperty(value = "Sentence length weeks.")
        private Integer weeks;

        @ApiModelProperty(value = "Sentence length days.")
        private Integer days;

        @ApiModelProperty(required = true, value = "Whether this is a life sentence.")
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
    @ApiModel(description = "Key Dates")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class KeyDates {

        @ApiModelProperty(value = "Sentence start date.", example = "2010-02-03", required = true)
        private LocalDate sentenceStartDate;
        @ApiModelProperty(value = "Effective sentence end date", example = "2020-02-03")
        private LocalDate effectiveSentenceEndDate;
        @ApiModelProperty(value = "ADA - days added to sentence term due to adjustments.", example = "5")
        private Integer additionalDaysAwarded;


        @ApiModelProperty(value = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.", example = "2020-04-01")
        private LocalDate nonDtoReleaseDate;
        @ApiModelProperty(value = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.", example = "CRD", allowableValues = "ARD,CRD,NPD,PRRD", required = true)
        private NonDtoReleaseDateType nonDtoReleaseDateType;
        @ApiModelProperty(value = "Confirmed release date for offender.", example = "2020-04-20")
        private LocalDate confirmedReleaseDate;
        @ApiModelProperty(value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
            "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", example = "2020-04-01")
        private LocalDate releaseDate;
        
        @ApiModelProperty(value = "SED - date on which sentence expires.", example = "2020-02-03")
        private LocalDate sentenceExpiryDate;
        @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.", example = "2020-02-03")
        private LocalDate automaticReleaseDate;
        @ApiModelProperty(value = "CRD - calculated conditional release date for offender.", example = "2020-02-03")
        private LocalDate conditionalReleaseDate;
        @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).", example = "2020-02-03")
        private LocalDate nonParoleDate;
        @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.", example = "2020-02-03")
        private LocalDate postRecallReleaseDate;
        @ApiModelProperty(value = "LED - date on which offender licence expires.", example = "2020-02-03")
        private LocalDate licenceExpiryDate;
        @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
        private LocalDate homeDetentionCurfewEligibilityDate;
        @ApiModelProperty(value = "PED - date on which offender is eligible for parole.", example = "2020-02-03")
        private LocalDate paroleEligibilityDate;
        @ApiModelProperty(value = "HDCAD - the offender's actual home detention curfew date.", example = "2020-02-03")
        private LocalDate homeDetentionCurfewActualDate;
        @ApiModelProperty(value = "APD - the offender's actual parole date.", example = "2020-02-03")
        private LocalDate actualParoleDate;
        @ApiModelProperty(value = "ROTL - the date on which offender will be released on temporary licence.", example = "2020-02-03")
        private LocalDate releaseOnTemporaryLicenceDate;
        @ApiModelProperty(value = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).", example = "2020-02-03")
        private LocalDate earlyRemovalSchemeEligibilityDate;
        @ApiModelProperty(value = "ETD - early term date for offender.", example = "2020-02-03")
        private LocalDate earlyTermDate;
        @ApiModelProperty(value = "MTD - mid term date for offender.", example = "2020-02-03")
        private LocalDate midTermDate;
        @ApiModelProperty(value = "LTD - late term date for offender.", example = "2020-02-03")
        private LocalDate lateTermDate;
        @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.", example = "2020-02-03")
        private LocalDate topupSupervisionExpiryDate;
        @ApiModelProperty(value = "Date on which minimum term is reached for parole (indeterminate/life sentences).", example = "2020-02-03")
        private LocalDate tariffDate;
        @ApiModelProperty(value = "DPRRD - Detention training order post recall release date", example = "2020-02-03")
        private LocalDate dtoPostRecallReleaseDate;
        @ApiModelProperty(value = "TERSED - Tariff early removal scheme eligibility date", example = "2020-02-03")
        private LocalDate tariffEarlyRemovalSchemeEligibilityDate;

        @ApiModelProperty(value = "Top-up supervision start date for offender - calculated as licence end date + 1 day or releaseDate if licence end date not set.", example = "2019-04-01")
        public LocalDate getTopupSupervisionStartDate() {
            if (getTopupSupervisionExpiryDate() == null) return null;
            if (getLicenceExpiryDate() != null) return getLicenceExpiryDate().plusDays(1);
            return getConditionalReleaseDate();
        }

        @ApiModelProperty(value = "Offender's home detention curfew end date - calculated as one day before the releaseDate.", example = "2019-04-01")
        public LocalDate getHomeDetentionCurfewEndDate() {
            if (getHomeDetentionCurfewActualDate() == null) return null;
            final var calcConditionalReleaseDate = getConditionalReleaseDate();
            return calcConditionalReleaseDate == null ? null : calcConditionalReleaseDate.minusDays(1);
        }

        public static KeyDates transform(final OffenderBooking booking, final Optional<SentenceCalculation> sentenceCalculation) {
                return sentenceCalculation.map(
                        sc -> KeyDates.builder()
                            .sentenceStartDate(booking.getSentenceStartDate().orElse(null))
                            .effectiveSentenceEndDate(sc.getEffectiveSentenceEndDate())
                            .additionalDaysAwarded(booking.getAdditionalDaysAwarded())
                            .automaticReleaseDate(sc.getAutomaticReleaseDate())
                            .conditionalReleaseDate(sc.getConditionalReleaseDate())
                            .sentenceExpiryDate(sc.getSentenceExpiryDate())
                            .postRecallReleaseDate(sc.getPostRecallReleaseDate())
                            .licenceExpiryDate(sc.getLicenceExpiryDate())
                            .homeDetentionCurfewEligibilityDate(sc.getHomeDetentionCurfewEligibilityDate())
                            .paroleEligibilityDate(sc.getParoleEligibilityDate())
                            .homeDetentionCurfewActualDate(sc.getHomeDetentionCurfewActualDate())
                            .actualParoleDate(sc.getActualParoleDate())
                            .releaseOnTemporaryLicenceDate(sc.getRotlOverridedDate())
                            .earlyRemovalSchemeEligibilityDate(sc.getErsedOverridedDate())
                            .tariffEarlyRemovalSchemeEligibilityDate(sc.getTersedOverridedDate())
                            .earlyTermDate(sc.getEarlyTermDate())
                            .midTermDate(sc.getMidTermDate())
                            .lateTermDate(sc.getLateTermDate())
                            .topupSupervisionExpiryDate(sc.getTopupSupervisionExpiryDate())
                            .tariffDate(sc.getTariffDate())
                            .dtoPostRecallReleaseDate(sc.getDtoPostRecallReleaseDate())
                            .nonParoleDate(sc.getNonParoleDate())
                            .nonDtoReleaseDate(sc.getNonDtoReleaseDate())
                            .nonDtoReleaseDateType(sc.getNonDtoReleaseDateType())
                            .releaseDate(booking.getReleaseDate(sentenceCalculation))
                            .confirmedReleaseDate(booking.getConfirmedReleaseDate().orElse(null))
                            .build())
                    .orElse(
                        KeyDates.builder()
                            .sentenceStartDate(booking.getSentenceStartDate().orElse(null))
                            .additionalDaysAwarded(booking.getAdditionalDaysAwarded())
                            .releaseDate(booking.getReleaseDate(sentenceCalculation))
                            .confirmedReleaseDate(booking.getConfirmedReleaseDate().orElse(null))
                            .build());
            }

    }

}
