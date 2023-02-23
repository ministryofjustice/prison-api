package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Offender Sentence Detail Dto
 **/
@SuppressWarnings("unused")
@Schema(description = "Offender Sentence Detail")
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

    @Schema(required = true, description = "Offender booking id.", example = "1212123")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Is this the most recent active booking", example = "true")
    @NotNull
    private Boolean mostRecentActiveBooking;

    @Schema(description = "Sentence start date.", example = "2019-04-02")
    private LocalDate sentenceStartDate;

    @Schema(description = "ADA - days added to sentence term due to adjustments.", example = "2")
    private Integer additionalDaysAwarded;

    @Schema(description = "SED - date on which sentence expires.", example = "2019-04-02")
    private LocalDate sentenceExpiryDate;

    @Schema(description = "ARD - calculated automatic (unconditional) release date for offender.", example = "2019-04-02")
    private LocalDate automaticReleaseDate;

    @Schema(description = "ARD (override) - automatic (unconditional) release override date for offender.", example = "2019-04-02")
    private LocalDate automaticReleaseOverrideDate;

    @Schema(description = "CRD - calculated conditional release date for offender.", example = "2019-04-02")
    private LocalDate conditionalReleaseDate;

    @Schema(description = "CRD (override) - conditional release override date for offender.", example = "2019-04-02")
    private LocalDate conditionalReleaseOverrideDate;

    @Schema(description = "NPD - calculated non-parole date for offender (relating to the 1991 act).", example = "2019-04-02")
    private LocalDate nonParoleDate;

    @Schema(description = "NPD (override) - non-parole override date for offender.", example = "2019-04-02")
    private LocalDate nonParoleOverrideDate;

    @Schema(description = "PRRD - calculated post-recall release date for offender.", example = "2019-04-02")
    private LocalDate postRecallReleaseDate;

    @Schema(description = "PRRD (override) - post-recall release override date for offender.", example = "2019-04-02")
    private LocalDate postRecallReleaseOverrideDate;

    @Schema(description = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.", example = "2019-04-02")
    private LocalDate nonDtoReleaseDate;

    @Schema(description = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.", example = "CRD")
    private NonDtoReleaseDateType nonDtoReleaseDateType;

    @Schema(description = "LED - date on which offender licence expires.", example = "2019-04-02")
    private LocalDate licenceExpiryDate;

    @Schema(description = "HDCED - date on which offender will be eligible for home detention curfew.", example = "2019-04-02")
    private LocalDate homeDetentionCurfewEligibilityDate;

    @Schema(description = "PED - date on which offender is eligible for parole.", example = "2019-04-02")
    private LocalDate paroleEligibilityDate;

    @Schema(description = "HDCAD - the offender's actual home detention curfew date.", example = "2019-04-02")
    private LocalDate homeDetentionCurfewActualDate;

    @Schema(description = "APD - the offender's actual parole date.", example = "2019-04-02")
    private LocalDate actualParoleDate;

    @Schema(description = "ROTL - the date on which offender will be released on temporary licence.", example = "2019-04-02")
    private LocalDate releaseOnTemporaryLicenceDate;

    @Schema(description = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).", example = "2019-04-02")
    private LocalDate earlyRemovalSchemeEligibilityDate;

    @Schema(description = "ETD - early term date for offender.", example = "2019-04-02")
    private LocalDate earlyTermDate;

    @Schema(description = "MTD - mid term date for offender.", example = "2019-04-02")
    private LocalDate midTermDate;

    @Schema(description = "LTD - late term date for offender.", example = "2019-04-02")
    private LocalDate lateTermDate;

    @Schema(description = "TUSED - top-up supervision expiry date for offender.")
    private LocalDate topupSupervisionExpiryDate;

    @Schema(description = "Confirmed release date for offender.", example = "2019-04-02")
    private LocalDate confirmedReleaseDate;

    @Schema(description = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
            "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", example = "2019-04-02")
    private LocalDate releaseDate;

    @Schema(description = "Date on which minimum term is reached for parole (indeterminate/life sentences).", example = "2019-04-02")
    private LocalDate tariffDate;

    @Schema(description = "DPRRD - Detention training order post recall release date", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDate;

    @Schema(description = "DPRRD - Detention training order post recall release date override", example = "2020-02-03")
    private LocalDate dtoPostRecallReleaseDateOverride;

    @Schema(description = "TERSED - Tariff early removal scheme eligibility date", example = "2020-02-03")
    private LocalDate tariffEarlyRemovalSchemeEligibilityDate;

    @Schema(description = "Effective sentence end date", example = "2020-02-03")

    private LocalDate effectiveSentenceEndDate;
    @Schema(description = "SED (calculated) - date on which sentence expires. (as calculated by NOMIS)", example = "2020-02-03")
    private LocalDate sentenceExpiryCalculatedDate;
    @Schema(description = "SED (override) - date on which sentence expires.", example = "2020-02-03")
    private LocalDate sentenceExpiryOverrideDate;
    @Schema(description = "LED (calculated) - date on which offender licence expires. (as calculated by NOMIS)", example = "2020-02-03")
    private LocalDate licenceExpiryCalculatedDate;
    @Schema(description = "LED (override) - date on which offender licence expires.", example = "2020-02-03")
    private LocalDate licenceExpiryOverrideDate;
    @Schema(description = "PED (calculated) - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityCalculatedDate;
    @Schema(description = "PED (override) - date on which offender is eligible for parole.", example = "2020-02-03")
    private LocalDate paroleEligibilityOverrideDate;

    @Schema(description = "TUSED (calculated) - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryCalculatedDate;

    @Schema(description = "TUSED (override) - top-up supervision expiry date for offender.", example = "2020-02-03")
    private LocalDate topupSupervisionExpiryOverrideDate;

    @Schema(description = "HDCED (calculated) - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityCalculatedDate;

    @Schema(description = "HDCED (override) - date on which offender will be eligible for home detention curfew.", example = "2020-02-03")
    private LocalDate homeDetentionCurfewEligibilityOverrideDate;

    @Schema(required = true, description = "Offender Unique Reference (NOMSID)", example = "A1234AA")
    @NotBlank
    private String offenderNo;

    @Schema(required = true, description = "First Name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(required = true, description = "Last Name", example = "Smith")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "Offender date of birth.", example = "1969-12-30")
    @NotNull
    private LocalDate dateOfBirth;

    @Schema(required = true, description = "Agency Id", example = "LEI")
    @NotBlank
    private String agencyLocationId;

    @Schema(required = true, description = "Agency Description", example = "HMP Leeds")
    @NotBlank
    private String agencyLocationDesc;

    @Schema(required = true, description = "Description of the location within the prison")
    @NotBlank
    private String internalLocationDesc;

    @Schema(description = "Identifier of facial image of offender.", example = "2342112")
    private Long facialImageId;

}
