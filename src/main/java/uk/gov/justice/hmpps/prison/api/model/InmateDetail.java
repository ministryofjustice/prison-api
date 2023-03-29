package uk.gov.justice.hmpps.prison.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;

/**
 * Inmate Detail
 **/
@SuppressWarnings("unused")
@Schema(description = "Inmate Detail")
@JsonInclude(Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class InmateDetail {

    @Schema(description = "Offender Unique Reference", example = "A1234AA")
    @NotBlank
    private String offenderNo;

    @Schema(description = "Offender Booking Id", example = "432132")
    private Long bookingId;

    @Schema(description = "Booking Number")
    private String bookingNo;

    @Schema(description = "Internal Offender ID")
    @NotBlank
    private Long offenderId;

    @Schema(description = "Internal Root Offender ID")
    @NotBlank
    private Long rootOffenderId;

    @Schema(description = "First Name")
    @NotBlank
    private String firstName;

    @Schema(description = "Middle Name(s)")
    private String middleName;

    @Schema(description = "Last Name")
    @NotBlank
    private String lastName;

    @Schema(description = "Date of Birth of prisoner", example = "1970-03-15")
    @NotNull
    private LocalDate dateOfBirth;

    @Schema(description = "Age of prisoner. Note: Full Details Only")
    private Integer age;

    @Schema(description = "Indicates that the person is currently in prison")
    @NotNull
    private boolean activeFlag;

    @Schema(description = "Image Id Ref of prisoner")
    private Long facialImageId;

    @Schema(description = "Identifier of agency to which the prisoner is associated.")
    private String agencyId;

    @Schema(description = "Identifier of living unit (e.g. cell) that prisoner is assigned to.")
    private Long assignedLivingUnitId;

    @Schema(description = "Religion of the prisoner")
    private String religion;

    @Schema(description = "Preferred spoken language")
    private String language;

    @Schema(description = "Interpreter required")
    private Boolean interpreterRequired;

    @Schema(description = "Preferred written language")
    private String writtenLanguage;

    @Schema(description = "List of Alerts")
    private List<String> alertsCodes;

    @Schema(description = "number of active alerts. Note: Full Details Only")
    private Long activeAlertCount;

    @Schema(description = "number of inactive alerts. Note: Full Details Only")
    private Long inactiveAlertCount;

    @Schema(description = "List of alert details")
    private List<Alert> alerts;

    @Schema(description = "Cell or location of the prisoner")
    private AssignedLivingUnit assignedLivingUnit;

    @Schema(description = "A set of physical attributes")
    private PhysicalAttributes physicalAttributes;

    @Schema(description = "List of physical characteristics")
    private List<PhysicalCharacteristic> physicalCharacteristics;

    @Schema(description = "List of profile information")
    private List<ProfileInformation> profileInformation;

    @Schema(description = "List of physical marks")
    private List<PhysicalMark> physicalMarks;

    @Schema(description = "List of assessments")
    private List<Assessment> assessments;

    @Schema(description = "CSRA (Latest assessment with cellSharing=true from list of assessments)")
    private String csra;

    @Schema(description = "The CSRA classification (calculated from the list of CSRA assessments)", example="STANDARD")
    private String csraClassificationCode;

    @Schema(description = "The date that the csraClassificationCode was assessed")
    private LocalDate csraClassificationDate;

    @Schema(description = "Category description (from list of assessments)")
    private String category;

    @Schema(description = "Category code (from list of assessments)")
    private String categoryCode;

    @Schema(description = "Place of birth", example = "WALES")
    private String birthPlace;

    @Schema(description = "Country of birth", example = "GBR")
    private String birthCountryCode;

    @Schema(description = "In/Out Status", example = "IN", allowableValues = {"IN","OUT","TRN"})
    private String inOutStatus;

    @Schema(description = "Identifiers. Note: Only returned when requesting extra details")
    private List<OffenderIdentifier> identifiers;

    @Schema(description = "Personal Care Needs. Note: Only returned when requesting extra details")
    private List<PersonalCareNeed> personalCareNeeds;

    @Schema(description = "Sentence Detail. Note: Only returned when requesting extra details")
    private SentenceCalcDates sentenceDetail;

    @Schema(description = "Offence History. Note: Only returned when requesting extra details")
    private List<OffenceHistoryDetail> offenceHistory;

    @Schema(description = "Current Sentence Terms. Note: Only returned when requesting extra details")
    private List<OffenderSentenceTerms> sentenceTerms;

    @Schema(description = "Aliases. Note: Only returned when requesting extra details")
    private List<Alias> aliases;

    @Schema(description = "Status of prisoner", example = "ACTIVE IN", allowableValues = {"ACTIVE IN","ACTIVE OUT"})
    private String status;

    @Schema(description = "Last movement status of the prison", example = "CRT-CA")
    private String statusReason;

    @Schema(description = "Last Movement Type Code of prisoner. Note: Reference Data from MOVE_TYPE Domain", example = "TAP", allowableValues = {"TAP","CRT","TRN","ADM","REL"})
    private String lastMovementTypeCode;

    @Schema(description = "Last Movement Reason of prisoner. Note: Reference Data from MOVE_RSN Domain", example = "CA")
    private String lastMovementReasonCode;

    @Schema(description = "Legal Status. Note: Only returned when requesting extra details", example = "REMAND")
    private LegalStatus legalStatus;

    @Schema(description = "Recall. Note: Only returned when requesting extra details", example = "true")
    private Boolean recall;

    @Schema(description = "The prisoner's imprisonment status. Note: Only returned when requesting extra details", example="LIFE")
    private String imprisonmentStatus;

    @Schema(description = "The prisoner's imprisonment status description. Note: Only returned when requesting extra details", example="Serving Life Imprisonment")
    private String imprisonmentStatusDescription;

    @Schema(description = "Date prisoner was received into the prison.", example="1980-01-01")
    private LocalDate receptionDate;

    @Schema(description = "current prison or outside with last movement information.", example="Outside - released from Leeds")
    private String locationDescription;

    @Schema(description = "the current prison id or the last prison before release", example="MDI")
    private String latestLocationId;

    public void setProfileInformation(final List<ProfileInformation> profileInformation) {
        this.profileInformation = profileInformation;
        updateReligion();
    }

    private void updateReligion() {
        if (profileInformation == null) {
            return;
        }
        religion = profileInformation
                .stream()
                .filter(pi -> "RELF".equals(pi.getType()))
                .findFirst()
                .map(ProfileInformation::getResultValue)
                .orElse(null);
    }

    public InmateDetail splitStatusReason() {
        final var splitStatusReason = StringUtils.split(statusReason, "-");
        if (splitStatusReason != null) {
            if (splitStatusReason.length >= 1) {
                lastMovementTypeCode = splitStatusReason[0];
            }
            if (splitStatusReason.length >= 2) {
                lastMovementReasonCode = splitStatusReason[1];
            }
        }
        return this;
    }

    public InmateDetail deriveStatus() {
        this.status = format("%s %s", activeFlag ? "ACTIVE" : "INACTIVE", inOutStatus);
        return this;
    }
}
