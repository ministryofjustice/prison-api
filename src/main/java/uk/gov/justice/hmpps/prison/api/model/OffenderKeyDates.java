package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender Key Dates")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderKeyDates {

    @Schema(description = "HDCED - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityDate;

    @Schema(description = "ETD - early term date for offender.", example = "2020-02-03")
    private LocalDate earlyTermDate;

    @Schema(description = "MTD - mid term date for offender.", example = "2020-02-03")
    private LocalDate midTermDate;

    @Schema(description = "LTD - late term date for offender.", example = "2020-02-03")
    private LocalDate lateTermDate;

    @Schema(description = "DPRRD - Detention training order post recall release date", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDate;

    @Schema(description = "ARD - calculated automatic (unconditional) release date for offender.", example = "2020-02-03")
    private LocalDate automaticReleaseDate;

    @Schema(description = "CRD - calculated conditional release date for offender.", example = "2020-02-03")
    private LocalDate conditionalReleaseDate;

    @Schema(description = "PED - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityDate;

    @Schema(description = "NPD - calculated non-parole date for offender (relating to the 1991 act).", example = "2020-02-03")
    private LocalDate nonParoleDate;

    @Schema(description = "LED - date on which offender licence expires.", example = "2020-02-03")
    private LocalDate licenceExpiryDate;

    @Schema(description = "PRRD - calculated post-recall release date for offender.", example = "2020-02-03")
    private LocalDate postRecallReleaseDate;

    @Schema(description = "SED - date on which sentence expires.", example = "2020-02-03")
    private LocalDate sentenceExpiryDate;

    @Schema(description = "TUSED - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryDate;

    @Schema(description = "ERSED - Early Removal Scheme Eligibility Date", example = "2020-02-03")
    private LocalDate earlyRemovalSchemeEligibilityDate;

    @Schema(requiredMode = REQUIRED, description = "Effective sentence end date.", example = "2020-02-03")
    private LocalDate effectiveSentenceEndDate;

    @Schema(requiredMode = REQUIRED, description = "Sentence length in the format 00 years/00 months/00 days.", example = "11/00/00")
    private String sentenceLength;

    @Schema(description = "HDCAD - Home Detention Curfew Approved date", example = "2020-02-03")
    private LocalDate homeDetentionCurfewApprovedDate;

    @Schema(description = "Tarrif - Tarrif date", example = "2020-02-03")
    private LocalDate tariffDate;

    @Schema(description = "Tarrif Expiry date", example = "2020-02-03")
    private LocalDate tariffExpiredRemovalSchemeEligibilityDate;

    @Schema(description = "APD - Approved Parole date", example = "2020-02-03")
    private LocalDate approvedParoleDate;


    @Schema(description = "ROTL = Release on temporary licence date", example = "2020-02-03")
    private LocalDate releaseOnTemporaryLicenceDate;
}
