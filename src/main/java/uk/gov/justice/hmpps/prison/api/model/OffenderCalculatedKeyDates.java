package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Offender Calculated Key Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class OffenderCalculatedKeyDates extends OffenderKeyDates {
    @Schema(description = "ROTL - Release on Temporary Licence", example = "2020-02-03")
    private LocalDate releaseOnTemporaryLicenceDate;
    @Schema(description = "Judicially imposed length in the format 00 years/00 months/00 days. Will default to 'sentenceLength'", example = "11/00/00")
    private String judiciallyImposedSentenceLength;
    @Schema(description = "Comments for the given calculation", example = "Calculated for new sentence")
    private String comment;
    @Schema(description = "The reason code for the calculation", example = "NEW")
    private String reasonCode;
    @Schema(description = "The date and time the calculation was recorded", example = "2017-10-31T01:30:00")
    private LocalDateTime calculatedAt;

    @Builder(builderMethodName = "offenderCalculatedKeyDates")
    public OffenderCalculatedKeyDates(LocalDate homeDetentionCurfewEligibilityDate, LocalDate earlyTermDate, LocalDate midTermDate, LocalDate lateTermDate, LocalDate dtoPostRecallReleaseDate, LocalDate automaticReleaseDate, LocalDate conditionalReleaseDate, LocalDate paroleEligibilityDate, LocalDate nonParoleDate, LocalDate licenceExpiryDate, LocalDate postRecallReleaseDate, LocalDate sentenceExpiryDate, LocalDate topupSupervisionExpiryDate, LocalDate earlyRemovalSchemeEligibilityDate, LocalDate effectiveSentenceEndDate, String sentenceLength, LocalDate releaseOnTemporaryLicenceDate, String judiciallyImposedSentenceLength, String comment, String reasonCode, LocalDate homeDetentionCurfewApprovedDate, LocalDate tariffDate, LocalDate tariffExpiredRemovalSchemeEligibilityDate, LocalDate approvedParoleDate, LocalDateTime calculatedAt) {
        super(homeDetentionCurfewEligibilityDate, earlyTermDate, midTermDate, lateTermDate, dtoPostRecallReleaseDate, automaticReleaseDate, conditionalReleaseDate, paroleEligibilityDate, nonParoleDate, licenceExpiryDate, postRecallReleaseDate, sentenceExpiryDate, topupSupervisionExpiryDate, earlyRemovalSchemeEligibilityDate, effectiveSentenceEndDate, sentenceLength, homeDetentionCurfewApprovedDate, tariffDate, tariffExpiredRemovalSchemeEligibilityDate, approvedParoleDate, releaseOnTemporaryLicenceDate);
        this.releaseOnTemporaryLicenceDate = releaseOnTemporaryLicenceDate;
        this.judiciallyImposedSentenceLength = judiciallyImposedSentenceLength;
        this.comment = comment;
        this.reasonCode = reasonCode;
        this.calculatedAt = calculatedAt;
    }
}
