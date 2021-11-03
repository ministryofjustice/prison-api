package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;

import javax.persistence.Column;
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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"offenderBooking", "sequence"}, callSuper = false)
@Table(name = "OFFENDER_SENTENCES")
@IdClass(OffenderSentence.PK.class)
@NamedEntityGraph(name = "sentence-entity-graph",
    attributeNodes = {
        @NamedAttributeNode(value = "offenderSentenceCharges", subgraph = "offender-sentence-charge-subgraph"),
        @NamedAttributeNode("calculationType"),
        @NamedAttributeNode("courtOrder"),
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
                @NamedAttributeNode("offence")
            }
        )
    }
)
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
        @JoinColumn(name = "SENTENCE_CALC_TYPE", referencedColumnName = "SENTENCE_CALC_TYPE"),
        @JoinColumn(name = "SENTENCE_CATEGORY", referencedColumnName = "SENTENCE_CATEGORY")
    })
    private SentenceCalcType calculationType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID"),
        @JoinColumn(name = "SENTENCE_SEQ", referencedColumnName = "SENTENCE_SEQ")
    })
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
    private List<OffenderSentenceCharge> offenderSentenceCharges;

    public OffenderSentenceAndOffences getSentenceAndOffenceDetail() {
        return OffenderSentenceAndOffences.builder()
            .bookingId(offenderBooking.getBookingId())
            .sentenceSequence(sequence)
            .consecutiveToSequence(consecutiveToSentenceSequence)
            .sentenceStatus(status)
            .sentenceCategory(calculationType.getCategory())
            .sentenceCalculationType(calculationType.getCalculationType())
            .sentenceTypeDescription(calculationType.getDescription())
            .sentenceDate(courtOrder == null ? null : courtOrder.getCourtDate())
            .years(terms == null ? 0 : terms.stream().mapToInt(val -> val.getYears() == null ? 0 : val.getYears()).sum())
            .months(terms == null ? 0 : terms.stream().mapToInt(val -> val.getMonths() == null ? 0 : val.getMonths()).sum())
            .weeks(terms == null ? 0 : terms.stream().mapToInt(val -> val.getWeeks() == null ? 0 : val.getWeeks()).sum())
            .days(terms == null ? 0 : terms.stream().mapToInt(val -> val.getDays() == null ? 0 : val.getDays()).sum())
            .offences(offenderSentenceCharges == null ? null : offenderSentenceCharges
                .stream()
                .map(i -> i.getOffenderCharge())
                .map(OffenderCharge::getOffenceDetail)
                .collect(Collectors.toList()))
            .build();
    }
}
