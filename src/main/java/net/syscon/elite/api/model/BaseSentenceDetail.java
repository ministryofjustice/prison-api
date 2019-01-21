package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;

/**
 * Basic Sentence Details
 **/
@ApiModel(description = "Sentence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class BaseSentenceDetail {

    @ApiModelProperty(value = "SED - date on which sentence expires.")
    protected LocalDate sentenceExpiryDate;
    @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.")
    protected LocalDate automaticReleaseDate;
    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.")
    protected LocalDate conditionalReleaseDate;
    @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).")
    protected LocalDate nonParoleDate;
    @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.")
    protected LocalDate postRecallReleaseDate;
    @ApiModelProperty(value = "LED - date on which offender licence expires.")
    protected LocalDate licenceExpiryDate;
    @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.")
    protected LocalDate homeDetentionCurfewEligibilityDate;
    @ApiModelProperty(value = "PED - date on which offender is eligible for parole.")
    protected LocalDate paroleEligibilityDate;
    @ApiModelProperty(value = "HDCAD - the offender's actual home detention curfew date.")
    protected LocalDate homeDetentionCurfewActualDate;
    @ApiModelProperty(value = "APD - the offender's actual parole date.")
    protected LocalDate actualParoleDate;
    @ApiModelProperty(value = "ROTL - the date on which offender will be released on temporary licence.")
    protected LocalDate releaseOnTemporaryLicenceDate;
    @ApiModelProperty(value = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).")
    protected LocalDate earlyRemovalSchemeEligibilityDate;
    @ApiModelProperty(value = "ETD - early term date for offender.")
    protected LocalDate earlyTermDate;
    @ApiModelProperty(value = "MTD - mid term date for offender.")
    protected LocalDate midTermDate;
    @ApiModelProperty(value = "LTD - late term date for offender.")
    protected LocalDate lateTermDate;
    @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.")
    protected LocalDate topupSupervisionExpiryDate;
    @ApiModelProperty(value = "Date on which minimum term is reached for parole (indeterminate/life sentences).")
    protected LocalDate tariffDate;

}
