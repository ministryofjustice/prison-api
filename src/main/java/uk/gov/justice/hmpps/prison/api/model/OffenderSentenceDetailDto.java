package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Offender Sentence Detail Dto
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class OffenderSentenceDetailDto {

    public enum NonDtoReleaseDateType {
        ARD, CRD, NPD, PRRD,
    }

    @ApiModelProperty(required = true, value = "Offender booking id.", example = "1212123")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Is this the most recent active booking", example = "true")
    @NotNull
    private Boolean mostRecentActiveBooking;

    @ApiModelProperty(value = "Sentence start date.", example = "2019-04-02")
    private LocalDate sentenceStartDate;

    @ApiModelProperty(value = "ADA - days added to sentence term due to adjustments.", example = "2")
    private Integer additionalDaysAwarded;

    @ApiModelProperty(value = "SED - date on which sentence expires.", example = "2019-04-02")
    private LocalDate sentenceExpiryDate;

    @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.", example = "2019-04-02")
    private LocalDate automaticReleaseDate;

    @ApiModelProperty(value = "ARD (override) - automatic (unconditional) release override date for offender.", example = "2019-04-02")
    private LocalDate automaticReleaseOverrideDate;

    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.", example = "2019-04-02")
    private LocalDate conditionalReleaseDate;

    @ApiModelProperty(value = "CRD (override) - conditional release override date for offender.", example = "2019-04-02")
    private LocalDate conditionalReleaseOverrideDate;

    @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).", example = "2019-04-02")
    private LocalDate nonParoleDate;

    @ApiModelProperty(value = "NPD (override) - non-parole override date for offender.", example = "2019-04-02")
    private LocalDate nonParoleOverrideDate;

    @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.", example = "2019-04-02")
    private LocalDate postRecallReleaseDate;

    @ApiModelProperty(value = "PRRD (override) - post-recall release override date for offender.", example = "2019-04-02")
    private LocalDate postRecallReleaseOverrideDate;

    @ApiModelProperty(value = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.", example = "2019-04-02")
    private LocalDate nonDtoReleaseDate;

    @ApiModelProperty(value = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.", example = "CRD")
    private NonDtoReleaseDateType nonDtoReleaseDateType;

    @ApiModelProperty(value = "LED - date on which offender licence expires.", example = "2019-04-02")
    private LocalDate licenceExpiryDate;

    @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.", example = "2019-04-02")
    private LocalDate homeDetentionCurfewEligibilityDate;

    @ApiModelProperty(value = "PED - date on which offender is eligible for parole.", example = "2019-04-02")
    private LocalDate paroleEligibilityDate;

    @ApiModelProperty(value = "HDCAD - the offender's actual home detention curfew date.", example = "2019-04-02")
    private LocalDate homeDetentionCurfewActualDate;

    @ApiModelProperty(value = "APD - the offender's actual parole date.", example = "2019-04-02")
    private LocalDate actualParoleDate;

    @ApiModelProperty(value = "ROTL - the date on which offender will be released on temporary licence.", example = "2019-04-02")
    private LocalDate releaseOnTemporaryLicenceDate;

    @ApiModelProperty(value = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).", example = "2019-04-02")
    private LocalDate earlyRemovalSchemeEligibilityDate;

    @ApiModelProperty(value = "ETD - early term date for offender.", example = "2019-04-02")
    private LocalDate earlyTermDate;

    @ApiModelProperty(value = "MTD - mid term date for offender.", example = "2019-04-02")
    private LocalDate midTermDate;

    @ApiModelProperty(value = "LTD - late term date for offender.", example = "2019-04-02")
    private LocalDate lateTermDate;

    @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.")
    private LocalDate topupSupervisionExpiryDate;

    @ApiModelProperty(value = "Confirmed release date for offender.", example = "2019-04-02")
    private LocalDate confirmedReleaseDate;

    @ApiModelProperty(value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
            "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", example = "2019-04-02")
    private LocalDate releaseDate;

    @ApiModelProperty(value = "Date on which minimum term is reached for parole (indeterminate/life sentences).", example = "2019-04-02")
    private LocalDate tariffDate;

    @ApiModelProperty(value = "DPRRD - Detention training order post recall release date", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDate;

    @ApiModelProperty(value = "DPRRD - Detention training order post recall release date override", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDateOverride;

    @ApiModelProperty(value = "TERSED - Tariff early removal scheme eligibility date", example = "2020-02-03")
    private LocalDate tariffEarlyRemovalSchemeEligibilityDate;

    @ApiModelProperty(value = "Effective sentence end date", example = "2020-02-03")
    private LocalDate effectiveSentenceEndDate;

    @ApiModelProperty(required = true, value = "Offender Unique Reference (NOMSID)", example = "A1234AA")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "First Name", example = "John")
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Last Name", example = "Smith")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Offender date of birth.", example = "1969-12-30")
    @NotNull
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "Agency Id", example = "LEI")
    @NotBlank
    private String agencyLocationId;

    @ApiModelProperty(required = true, value = "Agency Description", example = "HMP Leeds")
    @NotBlank
    private String agencyLocationDesc;

    @ApiModelProperty(required = true, value = "Description of the location within the prison")
    @NotBlank
    private String internalLocationDesc;

    @ApiModelProperty(value = "Identifier of facial image of offender.", example = "2342112")
    private Long facialImageId;

}
