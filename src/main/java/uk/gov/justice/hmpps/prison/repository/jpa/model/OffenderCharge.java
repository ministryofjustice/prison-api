package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import uk.gov.justice.hmpps.prison.api.model.OffenderOffence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" }, callSuper = false)
@Table(name = "OFFENDER_CHARGES")
@ToString(exclude = {"offenderBooking", "offenderCourtCase"})
@With
public class OffenderCharge extends AuditableEntity {

    private static final String ACTIVE = "A";

    @Id
    @Column(name = "OFFENDER_CHARGE_ID", nullable = false)
    @SequenceGenerator(name = "OFFENDER_CHARGE_ID", sequenceName = "OFFENDER_CHARGE_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_CHARGE_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CASE_ID", nullable = false)
    private OffenderCourtCase offenderCourtCase;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENCE_CODE", referencedColumnName="OFFENCE_CODE"),
        @JoinColumn(name="STATUTE_CODE", referencedColumnName="STATUTE_CODE")
    })
    private Offence offence;

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

    @OneToMany(mappedBy = "offenderCharge", fetch = FetchType.LAZY)
    @Default
    private List<OffenderSentenceCharge> offenderSentenceCharges = new ArrayList<>();

    public boolean isActive() {
        return ACTIVE.equals(chargeStatus);
    }

    public void setMostSeriousFlag(String mostSeriousFlag) {
        this.mostSeriousFlag = mostSeriousFlag;
    }

    public OffenderOffence getOffenceDetail() {
        return OffenderOffence.builder()
            .offenderChargeId(id)
            .offenceStartDate(dateOfOffence)
            .offenceEndDate(endDate)
            .offenceCode(offence.getCode())
            .offenceDescription(offence.getDescription())
            .indicators(offence.getOffenceIndicators().stream()
                .map(OffenceIndicator::getIndicatorCode)
                .toList())
            .build();
    }
}
