package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
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
@EqualsAndHashCode(of = { "id" }, callSuper = false)
@Table(name = "OFFENDER_SENTENCE_CHARGES")
@BatchSize(size = 25)
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
}
