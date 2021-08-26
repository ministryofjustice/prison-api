package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "offenderBooking", "sequence", "termSequence" }, callSuper = false)
@Table(name = "OFFENDER_SENTENCE_CHARGES")
@IdClass(OffenderSentenceCharge.PK.class)
public class OffenderSentenceCharge extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Integer sentenceSequence;
        private OffenderCharge offenderCharge;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "SENTENCE_SEQ")
    private Integer sentenceSequence;


    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_CHARGE_ID", nullable = false)
    private OffenderCharge offenderCharge;
}
