package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Base Sentence Calc Dates
 **/
@ApiModel(description = "Base Sentence Calc Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseSentenceCalcDates {

    @ApiModelProperty(value = "SED - date on which sentence expires.", position = 50, example = "2020-02-03")
    private LocalDate sentenceExpiryDate;
    @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.", position = 51, example = "2020-02-03")
    private LocalDate automaticReleaseDate;
    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.", position = 52, example = "2020-02-03")
    private LocalDate conditionalReleaseDate;
    @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).", position = 53, example = "2020-02-03")
    private LocalDate nonParoleDate;
    @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.", position = 54, example = "2020-02-03")
    private LocalDate postRecallReleaseDate;
    @ApiModelProperty(value = "LED - date on which offender licence expires.", position = 55, example = "2020-02-03")
    private LocalDate licenceExpiryDate;
    @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.", position = 56, example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityDate;
    @ApiModelProperty(value = "PED - date on which offender is eligible for parole.", position = 57, example = "2020-02-03")
    private LocalDate paroleEligibilityDate;
    @ApiModelProperty(value = "HDCAD - the offender's actual home detention curfew date.", position = 58, example = "2020-02-03")
    private LocalDate homeDetentionCurfewActualDate;
    @ApiModelProperty(value = "APD - the offender's actual parole date.", position = 59, example = "2020-02-03")
    private LocalDate actualParoleDate;
    @ApiModelProperty(value = "ROTL - the date on which offender will be released on temporary licence.", position = 60, example = "2020-02-03")
    private LocalDate releaseOnTemporaryLicenceDate;
    @ApiModelProperty(value = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).", position = 61, example = "2020-02-03")
    private LocalDate earlyRemovalSchemeEligibilityDate;
    @ApiModelProperty(value = "ETD - early term date for offender.", position = 62, example = "2020-02-03")
    private LocalDate earlyTermDate;
    @ApiModelProperty(value = "MTD - mid term date for offender.", position = 63, example = "2020-02-03")
    private LocalDate midTermDate;
    @ApiModelProperty(value = "LTD - late term date for offender.", position = 64, example = "2020-02-03")
    private LocalDate lateTermDate;
    @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.", position = 65, example = "2020-02-03")
    private LocalDate topupSupervisionExpiryDate;
    @ApiModelProperty(value = "Date on which minimum term is reached for parole (indeterminate/life sentences).", position = 66, example = "2020-02-03")
    private LocalDate tariffDate;
    @ApiModelProperty(value = "DPRRD - Detention training order post recall release date", position = 66, example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDate;
    @ApiModelProperty(value = "TERSED - Tariff early removal scheme eligibility date", position = 67, example = "2020-02-03")
    private LocalDate tariffEarlyRemovalSchemeEligibilityDate;
    @ApiModelProperty(value = "Effective sentence end date", position = 68, example = "2020-02-03")
    private LocalDate effectiveSentenceEndDate;
}
