package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Base Sentence Calc Dates
 **/
@Schema(description = "Base Sentence Calc Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseSentenceCalcDates {

    @Schema(description = "SED - date on which sentence expires.", example = "2020-02-03")
    private LocalDate sentenceExpiryDate;
    @Schema(description = "ARD - calculated automatic (unconditional) release date for offender.", example = "2020-02-03")
    private LocalDate automaticReleaseDate;
    @Schema(description = "CRD - calculated conditional release date for offender.", example = "2020-02-03")
    private LocalDate conditionalReleaseDate;
    @Schema(description = "NPD - calculated non-parole date for offender (relating to the 1991 act).", example = "2020-02-03")
    private LocalDate nonParoleDate;
    @Schema(description = "PRRD - calculated post-recall release date for offender.", example = "2020-02-03")
    private LocalDate postRecallReleaseDate;
    @Schema(description = "LED - date on which offender licence expires.", example = "2020-02-03")
    private LocalDate licenceExpiryDate;
    @Schema(description = "HDCED - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityDate;
    @Schema(description = "PED - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityDate;
    @Schema(description = "HDCAD - the offender's actual home detention curfew date.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewActualDate;
    @Schema(description = "APD - the offender's actual parole date.", example = "2020-02-03")
    private LocalDate actualParoleDate;
    @Schema(description = "ROTL - the date on which offender will be released on temporary licence.", example = "2020-02-03")
    private LocalDate releaseOnTemporaryLicenceDate;
    @Schema(description = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).", example = "2020-02-03")
    private LocalDate earlyRemovalSchemeEligibilityDate;
    @Schema(description = "ETD - early term date for offender.", example = "2020-02-03")
    private LocalDate earlyTermDate;
    @Schema(description = "MTD - mid term date for offender.", example = "2020-02-03")
    private LocalDate midTermDate;
    @Schema(description = "LTD - late term date for offender.", example = "2020-02-03")
    private LocalDate lateTermDate;
    @Schema(description = "TUSED - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryDate;
    @Schema(description = "Date on which minimum term is reached for parole (indeterminate/life sentences).", example = "2020-02-03")
    private LocalDate tariffDate;
    @Schema(description = "DPRRD - Detention training order post recall release date", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDate;
    @Schema(description = "TERSED - Tariff early removal scheme eligibility date", example = "2020-02-03")
    private LocalDate tariffEarlyRemovalSchemeEligibilityDate;
    @Schema(description = "Effective sentence end date", example = "2020-02-03")
    private LocalDate effectiveSentenceEndDate;
}
