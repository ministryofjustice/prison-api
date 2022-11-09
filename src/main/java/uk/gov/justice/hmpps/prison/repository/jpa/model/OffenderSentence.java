package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.annotations.BatchSize;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Table(name = "OFFENDER_SENTENCES")
@NamedEntityGraph(name = "sentence-entity-graph",
    attributeNodes = {
        @NamedAttributeNode(value = "offenderSentenceCharges", subgraph = "offender-sentence-charge-subgraph"),
        @NamedAttributeNode("calculationType"),
        @NamedAttributeNode("courtOrder"),
        @NamedAttributeNode("courtCase"),
    },
    subgraphs = {
        @NamedSubgraph(
            name = "offender-sentence-charge-subgraph",
            attributeNodes = {
                @NamedAttributeNode(value = "offenderCharge", subgraph = "offender-charge-subgraph")
            }
        ),
        @NamedSubgraph(
            name = "offender-charge-subgraph",
            attributeNodes = {
                @NamedAttributeNode(value = "offence")
            }
        )
    }
)
public class OffenderSentence extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", nullable = false)
        private Long offenderBookingId;
        @Column(name = "SENTENCE_SEQ", nullable = false)
        private Integer sequence;
    }

    @EmbeddedId
    private OffenderSentence.PK id;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", insertable = false, updatable = false)
    private OffenderBooking offenderBooking;

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
        @JoinColumn(name = "SENTENCE_CALC_TYPE", referencedColumnName = "SENTENCE_CALC_TYPE"),
        @JoinColumn(name = "SENTENCE_CATEGORY", referencedColumnName = "SENTENCE_CATEGORY")
    })
    @BatchSize(size = 25)
    private SentenceCalcType calculationType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID"),
        @JoinColumn(name = "SENTENCE_SEQ", referencedColumnName = "SENTENCE_SEQ")
    })
    @BatchSize(size = 25)
    private List<SentenceTerm> terms;

    @Column(name = "START_DATE")
    private LocalDate sentenceStartDate;

    @Column(name = "END_DATE")
    private LocalDate sentenceEndDate;

    @Column(name = "FINE_AMOUNT")
    private Double fineAmount;

    @Column(name = "LINE_SEQ")
    private Long lineSequence;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID"),
        @JoinColumn(name = "SENTENCE_SEQ", referencedColumnName = "SENTENCE_SEQ")
    })
    @BatchSize(size = 25)
    private List<OffenderSentenceCharge> offenderSentenceCharges;

    public Integer getSequence() {
        return id.sequence;
    }
    public OffenderSentenceAndOffences getSentenceAndOffenceDetail() {
        var sentenceDate = courtOrder == null ? null : courtOrder.getCourtDate();

        return OffenderSentenceAndOffences.builder()
            .bookingId(id.offenderBookingId)
            .sentenceSequence(id.sequence)
            .lineSequence(lineSequence)
            .caseSequence(courtCase == null ? null : courtCase.getCaseSeq())
            .caseReference(courtCase == null ? null : courtCase.getCaseInfoNumber())
            .courtDescription(courtCase == null || sentenceDate == null ? null :
                courtCase.getCourtEvents() == null ? null :
                    courtCase.getCourtEvents()
                        .stream()
                        .filter(val -> val.getEventDate().equals(sentenceDate))
                        .findAny()
                        .map(CourtEvent::getCourtLocation)
                        .map(AgencyLocation::getDescription)
                        .orElse(null))
            .consecutiveToSequence(consecutiveToSentenceSequence)
            .sentenceStatus(status)
            .sentenceCategory(calculationType.getCategory())
            .sentenceCalculationType(calculationType.getCalculationType())
            .sentenceTypeDescription(calculationType.getDescription())
            .sentenceDate(sentenceDate)
            .fineAmount(fineAmount)
            .terms(terms == null ? null : terms
                .stream()
                .map(term -> OffenderSentenceTerm.builder()
                    .years(term.getYears() == null ? 0 : term.getYears())
                    .months(term.getMonths() == null ? 0 : term.getMonths())
                    .weeks(term.getWeeks() == null ? 0 : term.getWeeks())
                    .days(term.getDays() == null ? 0 : term.getDays())
                    .code(term.getSentenceTermCode())
                    .build())
                .toList())
            .offences(offenderSentenceCharges == null ? null : offenderSentenceCharges
                .stream()
                .map(OffenderSentenceCharge::getOffenderCharge)
                .map(OffenderCharge::getOffenceDetail)
                .toList())

            .build();
    }
}
