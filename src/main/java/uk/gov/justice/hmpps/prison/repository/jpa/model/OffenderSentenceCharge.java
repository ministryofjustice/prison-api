package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" }, callSuper = false)
@With
@Table(name = "OFFENDER_SENTENCE_CHARGES")
public class OffenderSentenceCharge extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", nullable = false)
        private Long offenderBookingId;
        @Column(name = "SENTENCE_SEQ", nullable = false)
        private Integer sentenceSequence;
        @Column(name = "OFFENDER_CHARGE_ID", nullable = false)
        private Long offenderChargeId;
    }

    @EmbeddedId
    private OffenderSentenceCharge.PK id;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", insertable = false, updatable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_CHARGE_ID", insertable = false, updatable = false)
    private OffenderCharge offenderCharge;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID", insertable = false, updatable = false),
        @JoinColumn(name = "SENTENCE_SEQ", referencedColumnName = "SENTENCE_SEQ", insertable = false, updatable = false)
    })
    private OffenderSentence offenderSentence;
}
