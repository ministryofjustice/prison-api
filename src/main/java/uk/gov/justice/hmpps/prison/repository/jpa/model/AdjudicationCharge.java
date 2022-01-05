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
import javax.persistence.Table;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AGENCY_INCIDENT_CHARGES")
@ToString(of = {"id"})
public class AdjudicationCharge extends AuditableEntity {

    @EmbeddedId
    private PK id;

    @Data
    @Embeddable
    @EqualsAndHashCode(exclude = "adjudicationParty")
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class PK implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "AGENCY_INCIDENT_ID", nullable = false)
        @JoinColumn(name = "PARTY_SEQ", nullable = false)
        private AdjudicationParty adjudicationParty;

        @Column(name = "CHARGE_SEQ", nullable = false)
        private Long chargeSeq;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CHARGED_OIC_OFFENCE_ID")
    private AdjudicationOffenceType offenceType;

    public Long getSequenceNumber() {
        return id.getChargeSeq();
    }
}
