package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The represents the outcomes of offences in court and linked to {@link CourtEvent}s by the id.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "COURT_EVENT_CHARGES")
@IdClass(CourtEventCharge.Pk.class)
@ToString(exclude = {"offenderCharge"})
public class CourtEventCharge extends AuditableEntity {

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pk implements Serializable {
        @Id
        @ManyToOne(optional = false)
        @JoinColumn(name = "EVENT_ID", nullable = false)
        private CourtEvent courtEvent;

        @OneToOne(optional = false)
        @JoinColumn(name = "OFFENDER_CHARGE_ID", nullable = false)
        private OffenderCharge offenderCharge;
    }

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private CourtEvent courtEvent;

    @Id
    @OneToOne(optional = false)
    @JoinColumn(name = "OFFENDER_CHARGE_ID", nullable = false)
    private OffenderCharge offenderCharge;

    @Column(name = "PLEA_CODE")
    private String pleaCode;

    @Column(name = "RESULT_CODE_1")
    private String resultCodeOne;

    @Column(name = "RESULT_CODE_2")
    private String resultCodeTwo;

    @Column(name = "RESULT_CODE_1_INDICATOR")
    private String resultCodeOneIndicator;

    @Column(name = "RESULT_CODE_2_INDICATOR")
    private String resultCodeTwoIndicator;

    @Column(name = "MOST_SERIOUS_FLAG")
    private String mostSeriousFlag;

    @Column(name = "PROPERTY_VALUE")
    private BigDecimal propertyValue;

    @Column(name = "TOTAL_PROPERTY_VALUE")
    private BigDecimal totalPropertyValue;

    @Column(name = "NO_OF_OFFENCES")
    private Integer numberOfOffences;

    @Column(name = "OFFENCE_DATE")
    private LocalDate dateOfOffence;

    @Column(name = "OFFENCE_RANGE_DATE")
    private LocalDate endDate;

    @Column(name = "CJIT_OFFENCE_CODE_1")
    private String criminalJusticeInterventionsTeamCodeOne;

    @Column(name = "CJIT_OFFENCE_CODE_2")
    private String criminalJusticeInterventionsTeamCodeTwo;

    @Column(name = "CJIT_OFFENCE_CODE_3")
    private String criminalJusticeInterventionsTeamCodeThree;
}
