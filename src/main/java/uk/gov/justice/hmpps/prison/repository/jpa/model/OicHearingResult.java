package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@IdClass(OicHearingResult.PK.class)
@Table(name = "OIC_HEARING_RESULTS")
public class OicHearingResult extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        @Column(name = "OIC_HEARING_ID", updatable = false, insertable = false)
        private Long oicHearingId;

        @Column(name = "RESULT_SEQ", updatable = false, insertable = false)
        private Long resultSeq;
    }

    @Id
    private Long oicHearingId;

    @Id
    private Long resultSeq;

    @Column(name = "AGENCY_INCIDENT_ID", nullable = false)
    private Long agencyIncidentId;

    @Column(name = "CHARGE_SEQ", nullable = false)
    private Long chargeSeq;

    @Column(name = "PLEA_FINDING_CODE", nullable = false, length = 12)
    private String pleaFindingCode;

    @Column(name = "FINDING_CODE", nullable = false, length = 12)
    private String findingCode;

    @Column(name = "OIC_OFFENCE_ID", nullable = false)
    private Long oicOffenceId;
}
