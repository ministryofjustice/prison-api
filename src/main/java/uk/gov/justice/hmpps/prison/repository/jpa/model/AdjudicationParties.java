package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AGENCY_INCIDENT_PARTIES")
@ToString(of = {"id"})
public class AdjudicationParties extends ExtendedAuditableEntity {

    @EmbeddedId
    private PK id;

    @Data
    @Embeddable
    @EqualsAndHashCode
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
    private Long incidentId;

    @Column(name = "INCIDENT_ROLE")
    private String incidentRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    @MapsId("bookingId")
    private OffenderBooking offenderBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_ID")
    private Staff staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private Person person;
}
