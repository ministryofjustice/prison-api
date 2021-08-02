package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "OFFENDER_SENT_CALCULATIONS")
public class SentenceCalculation extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_SENT_CALCULATION_ID")
    private Long id;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "CALCULATION_DATE")
    private LocalDate calculationDate;

    @Column(name = "COMMENT_TEXT")
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_ID")
    private Staff staff;

    @Column(name = "CALC_REASON_CODE")
    private String reasonCode;

    @Column(name = "EFFECTIVE_SENTENCE_END_DATE")
    private LocalDate effectiveSentenceEndDate;

    @Column(name = "WORKFLOW_HISTORY_ID")
    private Long workflowHistoryId;

    @Column(name = "EFFECTIVE_SENTENCE_LENGTH")
    private String effectiveSentenceLength;

    @Column(name = "JUDICIALLY_IMPOSED_SENT_LENGTH")
    private String judiciallyImposedSentenceLength;

    @Column(name = "HDC_ELIGIBLE_WF")
    @Enumerated(EnumType.STRING)
    private ActiveFlag hdcEligible;

    @Column(name = "HDCED_CALCULATED_DATE")
    private LocalDate hdcedCalculatedDate;

    @Column(name = "HDCED_OVERRIDED_DATE")
    private LocalDate hdcedOverridedDate;

    @Column(name = "HDCAD_CALCULATED_DATE")
    private LocalDate hdcadCalculatedDate;

    @Column(name = "HDCAD_OVERRIDED_DATE")
    private LocalDate hdcadOverridedDate;

    @Column(name = "ETD_CALCULATED_DATE")
    private LocalDate etdCalculatedDate;

    @Column(name = "ETD_OVERRIDED_DATE")
    private LocalDate etdOverridedDate;

    @Column(name = "MTD_CALCULATED_DATE")
    private LocalDate mtdCalculatedDate;

    @Column(name = "MTD_OVERRIDED_DATE")
    private LocalDate mtdOverridedDate;

    @Column(name = "LTD_CALCULATED_DATE")
    private LocalDate ltdCalculatedDate;

    @Column(name = "LTD_OVERRIDED_DATE")
    private LocalDate ltdOverridedDate;

    @Column(name = "ARD_CALCULATED_DATE")
    private LocalDate ardCalculatedDate;

    @Column(name = "ARD_OVERRIDED_DATE")
    private LocalDate ardOverridedDate;

    @Column(name = "CRD_CALCULATED_DATE")
    private LocalDate crdCalculatedDate;

    @Column(name = "CRD_OVERRIDED_DATE")
    private LocalDate crdOverridedDate;

    @Column(name = "PED_CALCULATED_DATE")
    private LocalDate pedCalculatedDate;

    @Column(name = "PED_OVERRIDED_DATE")
    private LocalDate pedOverridedDate;

    @Column(name = "APD_CALCULATED_DATE")
    private LocalDate apdCalculatedDate;

    @Column(name = "APD_OVERRIDED_DATE")
    private LocalDate apdOverridedDate;

    @Column(name = "NPD_CALCULATED_DATE")
    private LocalDate npdCalculatedDate;

    @Column(name = "NPD_OVERRIDED_DATE")
    private LocalDate npdOverridedDate;

    @Column(name = "LED_CALCULATED_DATE")
    private LocalDate ledCalculatedDate;

    @Column(name = "LED_OVERRIDED_DATE")
    private LocalDate ledOverridedDate;

    @Column(name = "SED_CALCULATED_DATE")
    private LocalDate sedCalculatedDate;

    @Column(name = "SED_OVERRIDED_DATE")
    private LocalDate sedOverridedDate;

    @Column(name = "PRRD_CALCULATED_DATE")
    private LocalDate prrdCalculatedDate;

    @Column(name = "PRRD_OVERRIDED_DATE")
    private LocalDate prrdOverridedDate;

    @Column(name = "TARIFF_CALCULATED_DATE")
    private LocalDate tariffCalculatedDate;

    @Column(name = "TARIFF_OVERRIDED_DATE")
    private LocalDate tariffOverridedDate;

    @Column(name = "DPRRD_CALCULATED_DATE")
    private LocalDate dprrdCalculatedDate;

    @Column(name = "DPRRD_OVERRIDED_DATE")
    private LocalDate dprrdOverridedDate;

    @Column(name = "ERSED_OVERRIDED_DATE")
    private LocalDate ersedOverridedDate;

    @Column(name = "TERSED_OVERRIDED_DATE")
    private LocalDate tersedOverridedDate;

    @Column(name = "ROTL_OVERRIDED_DATE")
    private LocalDate rotlOverridedDate;

    @Column(name = "TUSED_CALCULATED_DATE")
    private LocalDate tusedCalculatedDate;

    @Column(name = "TUSED_OVERRIDED_DATE")
    private LocalDate tusedOverridedDate;

    @Column(name = "RECORD_DATETIME")
    private LocalDateTime recordedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_USER_ID")
    private StaffUserAccount recordedUser;


    public LocalDate getSentenceExpiryDate() {
        return sedOverridedDate != null ? sedOverridedDate : sedCalculatedDate;
    }

    public LocalDate getLicenceExpiryDate() {
        return ledOverridedDate != null ? ledOverridedDate : ledCalculatedDate;
    }
}
