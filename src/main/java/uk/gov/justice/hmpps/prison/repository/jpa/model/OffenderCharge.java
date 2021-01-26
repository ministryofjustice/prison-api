package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" }, callSuper = false)
@Table(name = "OFFENDER_CHARGES")
@ToString(exclude = {"offenderBooking", "offenderCourtCase"})
public class OffenderCharge extends AuditableEntity {

    private static final String ACTIVE = "A";

    @Id
    @Column(name = "OFFENDER_CHARGE_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CASE_ID", nullable = false)
    private OffenderCourtCase offenderCourtCase;

    @Column(name = "STATUTE_CODE", nullable = false)
    private String statuteCode;

    @Column(name = "OFFENCE_CODE", nullable = false)
    private String offenceCode;

    @Column(name = "NO_OF_OFFENCES")
    private Integer numberOfOffences;

    @Column(name = "OFFENCE_DATE")
    private LocalDate dateOfOffence;

    @Column(name = "OFFENCE_RANGE_DATE")
    private LocalDate endDate;

    @Column(name = "PLEA_CODE")
    private String pleaCode;

    @Column(name = "PROPERTY_VALUE")
    private BigDecimal propertyValue;

    @Column(name = "TOTAL_PROPERTY_VALUE")
    private BigDecimal totalPropertyValue;

    @Column(name = "CJIT_OFFENCE_CODE_1")
    private String criminalJusticeInterventionsTeamCodeOne;

    @Column(name = "CJIT_OFFENCE_CODE_2")
    private String criminalJusticeInterventionsTeamCodeTwo;

    @Column(name = "CJIT_OFFENCE_CODE_3")
    private String criminalJusticeInterventionsTeamCodeThree;

    @Column(name = "CHARGE_STATUS")
    private String chargeStatus;

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

    public boolean isActive() {
        return ACTIVE.equals(chargeStatus);
    }
}
