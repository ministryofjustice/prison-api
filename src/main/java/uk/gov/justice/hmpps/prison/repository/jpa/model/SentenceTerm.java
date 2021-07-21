package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "offenderBooking", "sequence", "termSequence" }, callSuper = false)
@Table(name = "OFFENDER_SENTENCE_TERMS")
@IdClass(SentenceTerm.PK.class)
public class SentenceTerm extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Integer sequence;
        private Integer termSequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "SENTENCE_SEQ")
    private Integer sequence;

    @Id
    @Column(name = "TERM_SEQ")
    private Integer termSequence;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENDER_BOOK_ID", referencedColumnName="OFFENDER_BOOK_ID", insertable = false, updatable = false),
        @JoinColumn(name="SENTENCE_SEQ", referencedColumnName="SENTENCE_SEQ", insertable = false, updatable = false)
    })
    private OffenderSentence offenderSentence;


    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "YEARS")
    private Integer years;

    @Column(name = "MONTHS")
    private Integer months;

    @Column(name = "WEEKS")
    private Integer weeks;

    @Column(name = "DAYS")
    private Integer days;

    @Column(name = "SENTENCE_TERM_CODE")
    private String sentenceTermCode;

    @Column(name = "LIFE_SENTENCE_FLAG")
    private String lifeSentenceFlag;

    public OffenderSentenceTerms getSentenceSummary() {
        return OffenderSentenceTerms.builder()
            .bookingId(offenderBooking.getBookingId())
            .sentenceSequence(getSequence())
            .termSequence(getTermSequence())
            .sentenceTermCode(getSentenceTermCode())
            .lifeSentence("Y".equals(getLifeSentenceFlag()))
            .days(getDays())
            .weeks(getWeeks())
            .months(getMonths())
            .years(getYears())
            .startDate(getStartDate())
            .caseId(String.valueOf(getOffenderSentence().getCourtCase().getId())) //TODO: this should be a number but API contract would break
            .consecutiveTo(getOffenderSentence().getConsecutiveToSentenceSequence())
            .sentenceType(getOffenderSentence().getCalculationType().getCalculationType())
            .sentenceStartDate(getOffenderSentence().getSentenceStartDate())
            .fineAmount(getOffenderSentence().getFineAmount())
            .lineSeq(getOffenderSentence().getLineSequence())
            .sentenceTypeDescription(getOffenderSentence().getCalculationType().getDescription())
            .build();
    }
}
