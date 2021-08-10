package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "offenderBooking", "sequence" }, callSuper = false)
@Table(name = "OFFENDER_SENTENCES")
@IdClass(OffenderSentence.PK.class)
public class OffenderSentence extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Integer sequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "SENTENCE_SEQ")
    private Integer sequence;

    @Column(name = "CONSEC_TO_SENTENCE_SEQ")
    private Integer consecutiveToSentenceSequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private CourtOrder courtOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CASE_ID", nullable = false)
    private OffenderCourtCase courtCase;

    @Column(name = "SENTENCE_STATUS")
    private String status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="SENTENCE_CALC_TYPE", referencedColumnName="SENTENCE_CALC_TYPE"),
        @JoinColumn(name="SENTENCE_CATEGORY", referencedColumnName="SENTENCE_CATEGORY")
    })
    private SentenceCalcType calculationType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENDER_BOOK_ID", referencedColumnName="OFFENDER_BOOK_ID"),
        @JoinColumn(name="SENTENCE_SEQ", referencedColumnName="SENTENCE_SEQ")
    })
    private List<SentenceTerm> terms;

    @Column(name = "START_DATE")
    private LocalDate sentenceStartDate;

    @Column(name = "FINE_AMOUNT")
    private Double fineAmount;

    @Column(name = "LINE_SEQ")
    private Long lineSequence;


}
