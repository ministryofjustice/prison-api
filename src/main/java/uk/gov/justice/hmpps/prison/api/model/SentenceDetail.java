package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Sentence Details
 **/
@ApiModel(description = "Sentence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class SentenceDetail extends BaseSentenceDetail {
    public enum NonDtoReleaseDateType {
        ARD, CRD, NPD, PRRD,
    }

    @ApiModelProperty(required = true, value = "Offender booking id.", position = 1, example = "1234123")
    @NotNull
    private Long bookingId;
    @ApiModelProperty(value = "Sentence start date.", position = 30, example = "2010-02-03", required = true)
    private LocalDate sentenceStartDate;
    @ApiModelProperty(value = "ADA - days added to sentence term due to adjustments.", position = 40, example = "5")
    private Integer additionalDaysAwarded;
    @ApiModelProperty(value = "ARD (override) - automatic (unconditional) release override date for offender.", position = 41, example = "2020-02-03")
    private LocalDate automaticReleaseOverrideDate;
    @ApiModelProperty(value = "CRD (override) - conditional release override date for offender.", position = 42, example = "2020-02-03")
    private LocalDate conditionalReleaseOverrideDate;
    @ApiModelProperty(value = "NPD (override) - non-parole override date for offender.", position = 43, example = "2020-02-03")
    private LocalDate nonParoleOverrideDate;
    @ApiModelProperty(value = "PRRD (override) - post-recall release override date for offender.", position = 44, example = "2020-04-01")
    private LocalDate postRecallReleaseOverrideDate;
    @ApiModelProperty(value = "DPRRD (override) - detention training order post-recall release override date for offender", position = 45, example = "2020-04-01")
    private LocalDate dtoPostRecallReleaseDateOverride;
    @ApiModelProperty(value = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.", position = 46, example = "2020-04-01")
    private LocalDate nonDtoReleaseDate;
    @ApiModelProperty(value = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.", position = 33, example = "CRD", allowableValues = "ARD,CRD,NPD,PRRD", required = true)
    private NonDtoReleaseDateType nonDtoReleaseDateType;
    @ApiModelProperty(value = "Confirmed release date for offender.", position = 31, example = "2020-04-20")
    private LocalDate confirmedReleaseDate;
    @ApiModelProperty(value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
        "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", position = 32, example = "2020-04-01")
    private LocalDate releaseDate;

    @Builder(builderMethodName = "sentenceDetailBuilder")
    public SentenceDetail(final LocalDate sentenceExpiryDate, final LocalDate automaticReleaseDate, final LocalDate conditionalReleaseDate, final LocalDate nonParoleDate, final LocalDate postRecallReleaseDate, final LocalDate licenceExpiryDate, final LocalDate homeDetentionCurfewEligibilityDate, final LocalDate paroleEligibilityDate, final LocalDate homeDetentionCurfewActualDate, final LocalDate actualParoleDate, final LocalDate releaseOnTemporaryLicenceDate, final LocalDate earlyRemovalSchemeEligibilityDate, final LocalDate earlyTermDate, final LocalDate midTermDate, final LocalDate lateTermDate, final LocalDate topupSupervisionExpiryDate, final LocalDate tariffDate, final LocalDate dtoPostRecallReleaseDate, final LocalDate tariffEarlyRemovalSchemeEligibilityDate, final LocalDate effectiveSentenceEndDate, @NotNull final Long bookingId, final LocalDate sentenceStartDate, final Integer additionalDaysAwarded, final LocalDate automaticReleaseOverrideDate, final LocalDate conditionalReleaseOverrideDate, final LocalDate nonParoleOverrideDate, final LocalDate postRecallReleaseOverrideDate, final LocalDate dtoPostRecallReleaseDateOverride, final LocalDate nonDtoReleaseDate, final NonDtoReleaseDateType nonDtoReleaseDateType, final LocalDate confirmedReleaseDate, final LocalDate releaseDate) {
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
        this.nonDtoReleaseDateType = nonDtoReleaseDateType;
        this.confirmedReleaseDate = confirmedReleaseDate;
        this.releaseDate = releaseDate;
    }

    @ApiModelProperty(value = "Top-up supervision start date for offender - calculated as licence end date + 1 day or releaseDate if licence end date not set.", example = "2019-04-01")
    public LocalDate getTopupSupervisionStartDate() {
        if (getTopupSupervisionExpiryDate() == null) return null;
        if (getLicenceExpiryDate() != null) return getLicenceExpiryDate().plusDays(1);
        return conditionalReleaseOverrideDate != null ? conditionalReleaseOverrideDate : getConditionalReleaseDate();
    }

    @ApiModelProperty(value = "Offender's home detention curfew end date - calculated as one day before the releaseDate.", example = "2019-04-01")
    public LocalDate getHomeDetentionCurfewEndDate() {
        if (getHomeDetentionCurfewActualDate() == null) return null;
        final var calcConditionalReleaseDate = conditionalReleaseOverrideDate != null ? conditionalReleaseOverrideDate : getConditionalReleaseDate();
        return calcConditionalReleaseDate == null ? null : calcConditionalReleaseDate.minusDays(1);
    }
}
