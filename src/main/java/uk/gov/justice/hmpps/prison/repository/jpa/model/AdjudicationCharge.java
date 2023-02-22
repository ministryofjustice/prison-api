package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    @EqualsAndHashCode
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

    @Column(name = "OIC_CHARGE_ID", nullable = true)
    private String oicChargeId;

    public Long getSequenceNumber() {
        return id.getChargeSeq();
    }
}
