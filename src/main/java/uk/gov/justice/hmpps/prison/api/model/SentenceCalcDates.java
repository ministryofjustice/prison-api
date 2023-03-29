package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.NonDtoReleaseDateType;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Sentence Details
 **/
@Schema(description = "Sentence Calculation Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class SentenceCalcDates extends BaseSentenceCalcDates {

    @Schema(description = "Offender booking id.", example = "1234123")
    @NotNull
    private Long bookingId;
    @Schema(description = "Sentence start date.", example = "2010-02-03")
    private LocalDate sentenceStartDate;
    @Schema(description = "ADA - days added to sentence term due to adjustments.", example = "5")
    private Integer additionalDaysAwarded;
    @Schema(description = "ARD (override) - automatic (unconditional) release override date for offender.", example = "2020-02-03")
    private LocalDate automaticReleaseOverrideDate;
    @Schema(description = "CRD (override) - conditional release override date for offender.", example = "2020-02-03")
    private LocalDate conditionalReleaseOverrideDate;
    @Schema(description = "NPD (override) - non-parole override date for offender.", example = "2020-02-03")
    private LocalDate nonParoleOverrideDate;
    @Schema(description = "PRRD (override) - post-recall release override date for offender.", example = "2020-04-01")
    private LocalDate postRecallReleaseOverrideDate;
    @Schema(description = "DPRRD (override) - detention training order post-recall release override date for offender", example = "2020-04-01")
    private LocalDate dtoPostRecallReleaseDateOverride;
    @Schema(description = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.", example = "2020-04-01")
    private LocalDate nonDtoReleaseDate;
    @Schema(description = "SED (calculated) - date on which sentence expires. (as calculated by NOMIS)", example = "2020-02-03")
    private LocalDate sentenceExpiryCalculatedDate;
    @Schema(description = "SED (override) - date on which sentence expires.", example = "2020-02-03")
    private LocalDate sentenceExpiryOverrideDate;
    @Schema(description = "LED (calculated) - date on which offender licence expires. (as calculated by NOMIS)", example = "2020-02-03")
    private LocalDate licenceExpiryCalculatedDate;
    @Schema(description = "LED (override) - date on which offender licence expires.", example = "2020-02-03")
    private LocalDate licenceExpiryOverrideDate;
    @Schema(description = "PED (calculated) - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityCalculatedDate;
    @Schema(description = "PED (override) - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityOverrideDate;

    @Schema(description = "TUSED (calculated) - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryCalculatedDate;

    @Schema(description = "TUSED (override) - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryOverrideDate;

    @Schema(description = "HDCED (calculated) - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityCalculatedDate;

    @Schema(description = "HDCED (override) - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityOverrideDate;
    @Schema(description = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.", example = "CRD")
    private NonDtoReleaseDateType nonDtoReleaseDateType;
    @Schema(description = "Confirmed release date for offender.", example = "2020-04-20")
    private LocalDate confirmedReleaseDate;
    @Schema(description = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
        "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", example = "2020-04-01")
    private LocalDate releaseDate;

    @Schema(description = "ETD Override - early term date for offender override date.", example = "2019-04-02")
    private LocalDate etdOverrideDate;

    @Schema(description = "ETD Override - early term date for offender calculated date.", example = "2019-04-02")
    private LocalDate etdCalculatedDate;

    @Schema(description = "MTD - mid term date for offender override date.", example = "2019-04-02")
    private LocalDate mtdOverrideDate;

    @Schema(description = "MTD - mid term date for offender calculated date.", example = "2019-04-02")
    private LocalDate mtdCalculatedDate;

    @Schema(description = "LTD - late term date for offender override date.", example = "2019-04-02")
    private LocalDate ltdOverrideDate;

    @Schema(description = "LTD - late term date for offender calculated date.", example = "2019-04-02")
    private LocalDate ltdCalculatedDate;
    @Builder(builderMethodName = "sentenceCalcDatesBuilder")
    public SentenceCalcDates(final LocalDate sentenceExpiryDate, final LocalDate automaticReleaseDate, final LocalDate conditionalReleaseDate, final LocalDate nonParoleDate, final LocalDate postRecallReleaseDate, final LocalDate licenceExpiryDate, final LocalDate homeDetentionCurfewEligibilityDate, final LocalDate paroleEligibilityDate, final LocalDate homeDetentionCurfewActualDate, final LocalDate actualParoleDate, final LocalDate releaseOnTemporaryLicenceDate, final LocalDate earlyRemovalSchemeEligibilityDate, final LocalDate earlyTermDate, final LocalDate midTermDate, final LocalDate lateTermDate, final LocalDate topupSupervisionExpiryDate, final LocalDate tariffDate, final LocalDate dtoPostRecallReleaseDate, final LocalDate tariffEarlyRemovalSchemeEligibilityDate, final LocalDate effectiveSentenceEndDate, @NotNull final Long bookingId, final LocalDate sentenceStartDate, final Integer additionalDaysAwarded, final LocalDate automaticReleaseOverrideDate, final LocalDate conditionalReleaseOverrideDate, final LocalDate nonParoleOverrideDate, final LocalDate postRecallReleaseOverrideDate, final LocalDate dtoPostRecallReleaseDateOverride, final LocalDate nonDtoReleaseDate, final LocalDate sentenceExpiryCalculatedDate, final LocalDate sentenceExpiryOverrideDate, final LocalDate licenceExpiryCalculatedDate, final LocalDate licenceExpiryOverrideDate, final LocalDate paroleEligibilityCalculatedDate, final LocalDate topupSupervisionExpiryCalculatedDate, final LocalDate topupSupervisionExpiryOverrideDate, final LocalDate homeDetentionCurfewEligibilityCalculatedDate, final LocalDate homeDetentionCurfewEligibilityOverrideDate, final LocalDate paroleEligibilityOverrideDate, final NonDtoReleaseDateType nonDtoReleaseDateType, final LocalDate confirmedReleaseDate, final LocalDate releaseDate,     final LocalDate etdOverrideDate, final LocalDate etdCalculatedDate, final LocalDate mtdOverrideDate, final LocalDate mtdCalculatedDate, final LocalDate ltdOverrideDate, final LocalDate ltdCalculatedDate) {
        super(sentenceExpiryDate, automaticReleaseDate, conditionalReleaseDate, nonParoleDate, postRecallReleaseDate, licenceExpiryDate, homeDetentionCurfewEligibilityDate, paroleEligibilityDate, homeDetentionCurfewActualDate, actualParoleDate, releaseOnTemporaryLicenceDate, earlyRemovalSchemeEligibilityDate, earlyTermDate, midTermDate, lateTermDate, topupSupervisionExpiryDate, tariffDate, dtoPostRecallReleaseDate, tariffEarlyRemovalSchemeEligibilityDate, effectiveSentenceEndDate);
        this.bookingId = bookingId;
        this.sentenceStartDate = sentenceStartDate;
        this.additionalDaysAwarded = additionalDaysAwarded;
        this.automaticReleaseOverrideDate = automaticReleaseOverrideDate;
        this.conditionalReleaseOverrideDate = conditionalReleaseOverrideDate;
        this.nonParoleOverrideDate = nonParoleOverrideDate;
        this.postRecallReleaseOverrideDate = postRecallReleaseOverrideDate;
        this.dtoPostRecallReleaseDateOverride = dtoPostRecallReleaseDateOverride;
        this.nonDtoReleaseDate = nonDtoReleaseDate;
        this.sentenceExpiryCalculatedDate = sentenceExpiryCalculatedDate;
        this.sentenceExpiryOverrideDate = sentenceExpiryOverrideDate;
        this.licenceExpiryCalculatedDate = licenceExpiryCalculatedDate;
        this.licenceExpiryOverrideDate = licenceExpiryOverrideDate;
        this.topupSupervisionExpiryCalculatedDate = topupSupervisionExpiryCalculatedDate;
        this.topupSupervisionExpiryOverrideDate = topupSupervisionExpiryOverrideDate;
        this.homeDetentionCurfewEligibilityCalculatedDate = homeDetentionCurfewEligibilityCalculatedDate;
        this.homeDetentionCurfewEligibilityOverrideDate = homeDetentionCurfewEligibilityOverrideDate;
        this.paroleEligibilityCalculatedDate = paroleEligibilityCalculatedDate;
        this.paroleEligibilityOverrideDate = paroleEligibilityOverrideDate;
        this.nonDtoReleaseDateType = nonDtoReleaseDateType;
        this.confirmedReleaseDate = confirmedReleaseDate;
        this.releaseDate = releaseDate;
        this.etdOverrideDate = etdOverrideDate;
        this.etdCalculatedDate = etdCalculatedDate;
        this.mtdOverrideDate = mtdOverrideDate;
        this.mtdCalculatedDate = mtdCalculatedDate;
        this.ltdOverrideDate = ltdOverrideDate;
        this.ltdCalculatedDate = ltdCalculatedDate;
    }

    @Schema(description = "Top-up supervision start date for offender - calculated as licence end date + 1 day or releaseDate if licence end date not set.", example = "2019-04-01")
    public LocalDate getTopupSupervisionStartDate() {
        if (getTopupSupervisionExpiryDate() == null) return null;
        if (getLicenceExpiryDate() != null) return getLicenceExpiryDate().plusDays(1);
        return conditionalReleaseOverrideDate != null ? conditionalReleaseOverrideDate : getConditionalReleaseDate();
    }

    @Schema(description = "Offender's home detention curfew end date - calculated as one day before the releaseDate.", example = "2019-04-01")
    public LocalDate getHomeDetentionCurfewEndDate() {
        if (getHomeDetentionCurfewActualDate() == null) return null;
        final var calcConditionalReleaseDate = conditionalReleaseOverrideDate != null ? conditionalReleaseOverrideDate : getConditionalReleaseDate();
        return calcConditionalReleaseDate == null ? null : calcConditionalReleaseDate.minusDays(1);
    }
}
