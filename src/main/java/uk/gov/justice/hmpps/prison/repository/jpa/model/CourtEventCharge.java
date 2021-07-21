package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * This represents the outcomes of offences in after going to court and linked to a {@link CourtEvent} by the id.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Table(name = "COURT_EVENT_CHARGES")
@ToString(exclude = {"eventAndCharge"})
public class CourtEventCharge extends AuditableEntity {

    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    @Getter
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "EVENT_ID", nullable = false)
        private CourtEvent courtEvent;

        @OneToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "OFFENDER_CHARGE_ID", nullable = false)
        private OffenderCharge offenderCharge;
    }

    @EmbeddedId
    private CourtEventCharge.Pk eventAndCharge;

    @Column(name = "PLEA_CODE")
    private String pleaCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESULT_CODE_1")
    private OffenceResult resultCodeOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESULT_CODE_2")
    private OffenceResult resultCodeTwo;

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

    @Builder
    private CourtEventCharge(final OffenderCharge offenderCharge, final CourtEvent courtEvent) {
        Objects.requireNonNull(offenderCharge, "Offender Charge cannot be null.");
        Objects.requireNonNull(courtEvent, "Court Event cannot be null");

        this.eventAndCharge = new CourtEventCharge.Pk(courtEvent, offenderCharge);
        this.pleaCode = offenderCharge.getPleaCode();
        this.resultCodeOne = offenderCharge.getResultCodeOne();
        this.resultCodeTwo = offenderCharge.getResultCodeTwo();
        this.resultCodeOneIndicator = offenderCharge.getResultCodeOneIndicator();
        this.resultCodeTwoIndicator = offenderCharge.getResultCodeTwoIndicator();
        this.mostSeriousFlag = offenderCharge.getMostSeriousFlag();
        this.propertyValue = offenderCharge.getPropertyValue();
        this.totalPropertyValue = offenderCharge.getTotalPropertyValue();
        this.numberOfOffences = offenderCharge.getNumberOfOffences();
        this.dateOfOffence = offenderCharge.getDateOfOffence();
        this.endDate = offenderCharge.getEndDate();
        this.criminalJusticeInterventionsTeamCodeOne = offenderCharge.getCriminalJusticeInterventionsTeamCodeOne();
        this.criminalJusticeInterventionsTeamCodeTwo = offenderCharge.getCriminalJusticeInterventionsTeamCodeTwo();
        this.criminalJusticeInterventionsTeamCodeThree = offenderCharge.getCriminalJusticeInterventionsTeamCodeThree();
    }
}
