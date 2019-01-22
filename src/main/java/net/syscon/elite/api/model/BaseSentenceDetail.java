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

    @ApiModelProperty(value = "SED - date on which sentence expires.", position = 50)
    private LocalDate sentenceExpiryDate;
    @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.", position = 51)
    private LocalDate automaticReleaseDate;
    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.", position = 52)
    private LocalDate conditionalReleaseDate;
    @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).", position = 53)
    private LocalDate nonParoleDate;
    @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.", position = 54)
    private LocalDate postRecallReleaseDate;
    @ApiModelProperty(value = "LED - date on which offender licence expires.", position = 55)
    private LocalDate licenceExpiryDate;
    @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.", position = 56)
    private LocalDate homeDetentionCurfewEligibilityDate;
    @ApiModelProperty(value = "PED - date on which offender is eligible for parole.", position = 57)
    private LocalDate paroleEligibilityDate;
    @ApiModelProperty(value = "HDCAD - the offender's actual home detention curfew date.", position = 58)
    private LocalDate homeDetentionCurfewActualDate;
    @ApiModelProperty(value = "APD - the offender's actual parole date.", position = 59)
    private LocalDate actualParoleDate;
    @ApiModelProperty(value = "ROTL - the date on which offender will be released on temporary licence.", position = 60)
    private LocalDate releaseOnTemporaryLicenceDate;
    @ApiModelProperty(value = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).", position = 61)
    private LocalDate earlyRemovalSchemeEligibilityDate;
    @ApiModelProperty(value = "ETD - early term date for offender.", position = 62)
    private LocalDate earlyTermDate;
    @ApiModelProperty(value = "MTD - mid term date for offender.", position = 63)
    private LocalDate midTermDate;
    @ApiModelProperty(value = "LTD - late term date for offender.", position = 64)
    private LocalDate lateTermDate;
    @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.", position = 65)
    private LocalDate topupSupervisionExpiryDate;
    @ApiModelProperty(value = "Date on which minimum term is reached for parole (indeterminate/life sentences).", position = 66)
    private LocalDate tariffDate;

}
