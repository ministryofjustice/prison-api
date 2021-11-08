package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Type;
import uk.gov.justice.hmpps.prison.service.support.NonDtoReleaseDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.CalcReasonType.CALC_REASON_TYPE;

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
    @SequenceGenerator(name = "OFFENDER_SENT_CALCULATION_ID", sequenceName = "OFFENDER_SENT_CALCULATION_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_SENT_CALCULATION_ID")
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

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CALC_REASON_TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "CALC_REASON_CODE", referencedColumnName = "code"))
    })
    private CalcReasonType calcReasonType;

    @Column(name = "EFFECTIVE_SENTENCE_END_DATE")
    private LocalDate effectiveSentenceEndDate;

    @Column(name = "WORKFLOW_HISTORY_ID")
    private Long workflowHistoryId;

    @Column(name = "EFFECTIVE_SENTENCE_LENGTH")
    private String effectiveSentenceLength;

    @Column(name = "JUDICIALLY_IMPOSED_SENT_LENGTH")
    private String judiciallyImposedSentenceLength;

    @Column(name = "HDC_ELIGIBLE_WF")
    @Type(type="yes_no")
    private boolean hdcEligible;

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

    public LocalDate getActualParoleDate() {
        return apdOverridedDate != null ? apdOverridedDate : apdCalculatedDate;
    }

    public LocalDate getAutomaticReleaseDate() {
        return ardOverridedDate != null ? ardOverridedDate : ardCalculatedDate;
    }

    public LocalDate getConditionalReleaseDate() {
        return crdOverridedDate != null ? crdOverridedDate : crdCalculatedDate;
    }

    public LocalDate getNonParoleDate() {
        return npdOverridedDate != null ? npdOverridedDate : npdCalculatedDate;
    }

    public LocalDate getPostRecallReleaseDate() {
        return prrdOverridedDate != null ? prrdOverridedDate : prrdCalculatedDate;
    }

    public LocalDate getHomeDetentionCurfewActualDate() {
        return hdcadOverridedDate != null ? hdcadOverridedDate : hdcadCalculatedDate;
    }

    public LocalDate getHomeDetentionCurfewEligibilityDate() {
        return hdcedOverridedDate != null ? hdcedOverridedDate : hdcedCalculatedDate;
    }

    public LocalDate getEarlyTermDate() {
        return etdOverridedDate != null ? etdOverridedDate : etdCalculatedDate;
    }

    public LocalDate getMidTermDate() {
        return mtdOverridedDate != null ? mtdOverridedDate : mtdCalculatedDate;
    }

    public LocalDate getLateTermDate() {
        return ltdOverridedDate != null ? ltdOverridedDate : ltdCalculatedDate;
    }

    public LocalDate getParoleEligibilityDate() {
        return pedOverridedDate != null ? pedOverridedDate : pedCalculatedDate;
    }

    public LocalDate getTopupSupervisionExpiryDate() {
        return tusedOverridedDate != null ? tusedOverridedDate : tusedCalculatedDate;
    }

    public LocalDate getTariffDate() {
        return tariffOverridedDate != null ? tariffOverridedDate : tariffCalculatedDate;
    }

    public LocalDate getDtoPostRecallReleaseDate() {
        return dprrdOverridedDate != null ? dprrdOverridedDate : dprrdCalculatedDate;
    }

    public LocalDate getNonDtoReleaseDate() {
        return deriveNonDtoReleaseDate(buildKeyDates()).map(NonDtoReleaseDate::getReleaseDate).orElse(null);
    }

    public NonDtoReleaseDateType getNonDtoReleaseDateType() {
        return deriveNonDtoReleaseDate(buildKeyDates()).map(NonDtoReleaseDate::getReleaseDateType).orElse(null);
    }

    private KeyDateValues buildKeyDates() {
        return new KeyDateValues(
            getArdCalculatedDate(),
            getArdOverridedDate(),
            getCrdCalculatedDate(),
            getCrdOverridedDate(),
            getNpdCalculatedDate(),
            getNpdOverridedDate(),
            getPrrdCalculatedDate(),
            getPrrdOverridedDate(),
            getActualParoleDate(),
            getHomeDetentionCurfewActualDate(),
            getMidTermDate(),
            null);
    }


    public static Optional<NonDtoReleaseDate> deriveNonDtoReleaseDate(final KeyDateValues sentenceDetail) {
        final List<NonDtoReleaseDate> nonDtoReleaseDates = new ArrayList<>();

        if (Objects.nonNull(sentenceDetail)) {
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.automaticReleaseDate(), NonDtoReleaseDateType.ARD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.automaticReleaseOverrideDate(), NonDtoReleaseDateType.ARD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.conditionalReleaseDate(), NonDtoReleaseDateType.CRD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.conditionalReleaseOverrideDate(), NonDtoReleaseDateType.CRD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.nonParoleDate(), NonDtoReleaseDateType.NPD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.nonParoleOverrideDate(), NonDtoReleaseDateType.NPD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.postRecallReleaseDate(), NonDtoReleaseDateType.PRRD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.postRecallReleaseOverrideDate(), NonDtoReleaseDateType.PRRD, true);

            Collections.sort(nonDtoReleaseDates);
        }

        return nonDtoReleaseDates.isEmpty() ? Optional.empty() : Optional.of(nonDtoReleaseDates.get(0));
    }

    private static void addReleaseDate(final List<NonDtoReleaseDate> nonDtoReleaseDates, final LocalDate releaseDate,
                                       final NonDtoReleaseDateType releaseDateType, final boolean isOverride) {

        if (Objects.nonNull(releaseDate)) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(releaseDateType, releaseDate, isOverride));
        }
    }


    public enum NonDtoReleaseDateType {
        ARD, CRD, NPD, PRRD,
    }

    public record KeyDateValues(LocalDate automaticReleaseDate, LocalDate automaticReleaseOverrideDate,
                                LocalDate conditionalReleaseDate, LocalDate conditionalReleaseOverrideDate,
                                LocalDate nonParoleDate, LocalDate nonParoleOverrideDate,
                                LocalDate postRecallReleaseDate, LocalDate postRecallReleaseOverrideDate,
                                LocalDate actualParoleDate, LocalDate homeDetentionCurfewActualDate, LocalDate midTermDate,
                                LocalDate confirmedReleaseDate) {
    }

}
