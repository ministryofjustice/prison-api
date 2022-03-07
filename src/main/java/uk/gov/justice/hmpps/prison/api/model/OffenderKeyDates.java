package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Offender Key Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderKeyDates {

    @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityDate;

    @ApiModelProperty(value = "ETD - early term date for offender.", example = "2020-02-03")
    private LocalDate earlyTermDate;

    @ApiModelProperty(value = "MTD - mid term date for offender.", example = "2020-02-03")
    private LocalDate midTermDate;

    @ApiModelProperty(value = "LTD - late term date for offender.", example = "2020-02-03")
    private LocalDate lateTermDate;

    @ApiModelProperty(value = "DPRRD - Detention training order post recall release date", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDate;

    @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.", example = "2020-02-03")
    private LocalDate automaticReleaseDate;

    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.", example = "2020-02-03")
    private LocalDate conditionalReleaseDate;

    @ApiModelProperty(value = "PED - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityDate;

    @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).", example = "2020-02-03")
    private LocalDate nonParoleDate;

    @ApiModelProperty(value = "LED - date on which offender licence expires.", example = "2020-02-03")
    private LocalDate licenceExpiryDate;

    @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.", example = "2020-02-03")
    private LocalDate postRecallReleaseDate;

    @ApiModelProperty(value = "SED - date on which sentence expires.", example = "2020-02-03")
    private LocalDate sentenceExpiryDate;

    @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryDate;

    @ApiModelProperty(required = true, value = "Effective sentence end date.", example = "2020-02-03")
    private LocalDate effectiveSentenceEndDate;

    @ApiModelProperty(required = true, value = "Sentence length in the format 00 years/00 months/00 days.", example = "11/00/00")
    private String sentenceLength;
}
