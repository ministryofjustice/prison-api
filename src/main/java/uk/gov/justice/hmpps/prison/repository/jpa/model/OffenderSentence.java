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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

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
        private Long sequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "SENTENCE_SEQ")
    private Long sequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private CourtOrder courtOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CASE_ID", nullable = false)
    private OffenderCourtCase courtCase;

    @Column(name = "SENTENCE_CALC_TYPE")
    private String calcType;

    @Column(name = "SENTENCE_STATUS")
    private String status;
}
