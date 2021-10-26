package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AGENCY_INCIDENT_PARTIES")
@ToString(of = {"id"})
public class AdjudicationParty extends AuditableEntity {

    @EmbeddedId
    private PK id;

    @Data
    @Embeddable
    @EqualsAndHashCode(exclude = "adjudication")
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class PK implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "AGENCY_INCIDENT_ID", nullable = false)
        private Adjudication adjudication;

        @Column(name = "PARTY_SEQ", nullable = false)
        private Long partySeq;
    }

    @Column(name = "OIC_INCIDENT_ID")
    private Long adjudicationNumber;

    @Column(name = "INCIDENT_ROLE")
    private String incidentRole;

    @Column(name = "PARTY_ADDED_DATE", nullable = false)
    private LocalDate partyAddedDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AdjudicationActionCode.TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "ACTION_CODE", referencedColumnName = "code"))
    })
    private AdjudicationActionCode actionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    private OffenderBooking offenderBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_ID")
    private Staff staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private Person person;
}
