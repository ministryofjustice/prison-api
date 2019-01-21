package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
@ToString
public class SentenceDetail extends BaseSentenceDetail {
    public enum NonDtoReleaseDateType {
        ARD,  CRD,  NPD,  PRRD,
    }

    @ApiModelProperty(required = true, value = "Offender booking id.")
    @NotNull
    private Long bookingId;
    @ApiModelProperty(value = "Sentence start date.")
    private LocalDate sentenceStartDate;
    @ApiModelProperty(value = "ADA - days added to sentence term due to adjustments.")
    private Integer additionalDaysAwarded;
    @ApiModelProperty(value = "ARD (override) - automatic (unconditional) release override date for offender.")
    private LocalDate automaticReleaseOverrideDate;
    @ApiModelProperty(value = "CRD (override) - conditional release override date for offender.")
    private LocalDate conditionalReleaseOverrideDate;
    @ApiModelProperty(value = "NPD (override) - non-parole override date for offender.")
    private LocalDate nonParoleOverrideDate;
    @ApiModelProperty(value = "PRRD (override) - post-recall release override date for offender.")
    private LocalDate postRecallReleaseOverrideDate;
    @ApiModelProperty(value = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.")
    private LocalDate nonDtoReleaseDate;
    @ApiModelProperty(value = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.")
    private NonDtoReleaseDateType nonDtoReleaseDateType;
    @ApiModelProperty(value = "Confirmed release date for offender.")
    private LocalDate confirmedReleaseDate;
    @ApiModelProperty(value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm.")
    private LocalDate releaseDate;

    @Builder(builderMethodName = "sentenceDetailBuilder")
    public SentenceDetail(LocalDate sentenceExpiryDate, LocalDate automaticReleaseDate, LocalDate conditionalReleaseDate, LocalDate nonParoleDate, LocalDate postRecallReleaseDate, LocalDate licenceExpiryDate, LocalDate homeDetentionCurfewEligibilityDate, LocalDate paroleEligibilityDate, LocalDate homeDetentionCurfewActualDate, LocalDate actualParoleDate, LocalDate releaseOnTemporaryLicenceDate, LocalDate earlyRemovalSchemeEligibilityDate, LocalDate earlyTermDate, LocalDate midTermDate, LocalDate lateTermDate, LocalDate topupSupervisionExpiryDate, LocalDate tariffDate, @NotNull Long bookingId, LocalDate sentenceStartDate, Integer additionalDaysAwarded, LocalDate automaticReleaseOverrideDate, LocalDate conditionalReleaseOverrideDate, LocalDate nonParoleOverrideDate, LocalDate postRecallReleaseOverrideDate, LocalDate nonDtoReleaseDate, NonDtoReleaseDateType nonDtoReleaseDateType, LocalDate confirmedReleaseDate, LocalDate releaseDate) {
        super(sentenceExpiryDate, automaticReleaseDate, conditionalReleaseDate, nonParoleDate, postRecallReleaseDate, licenceExpiryDate, homeDetentionCurfewEligibilityDate, paroleEligibilityDate, homeDetentionCurfewActualDate, actualParoleDate, releaseOnTemporaryLicenceDate, earlyRemovalSchemeEligibilityDate, earlyTermDate, midTermDate, lateTermDate, topupSupervisionExpiryDate, tariffDate);
        this.bookingId = bookingId;
        this.sentenceStartDate = sentenceStartDate;
        this.additionalDaysAwarded = additionalDaysAwarded;
        this.automaticReleaseOverrideDate = automaticReleaseOverrideDate;
        this.conditionalReleaseOverrideDate = conditionalReleaseOverrideDate;
        this.nonParoleOverrideDate = nonParoleOverrideDate;
        this.postRecallReleaseOverrideDate = postRecallReleaseOverrideDate;
        this.nonDtoReleaseDate = nonDtoReleaseDate;
        this.nonDtoReleaseDateType = nonDtoReleaseDateType;
        this.confirmedReleaseDate = confirmedReleaseDate;
        this.releaseDate = releaseDate;
    }
}
