package uk.gov.justice.hmpps.prison.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.hmpps.prison.api.model.LegalStatusCalc.LegalStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;

/**
 * Inmate Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Inmate Detail")
@JsonInclude(Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class InmateDetail {

    @ApiModelProperty(required = true, value = "Offender Unique Reference", position = 1, example = "A1234AA")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(value = "Offender Booking Id", position = 2, example = "432132")
    private Long bookingId;

    @ApiModelProperty(value = "Booking Number", position = 3)
    private String bookingNo;

    @ApiModelProperty(required = true, value = "Internal Offender ID", position = 4)
    @NotBlank
    private Long offenderId;

    @ApiModelProperty(required = true, value = "Internal Root Offender ID", position = 5)
    @NotBlank
    private Long rootOffenderId;

    @ApiModelProperty(required = true, value = "First Name", position = 6)
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "Middle Name(s)", position = 7)
    private String middleName;

    @ApiModelProperty(required = true, value = "Last Name", position = 8)
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Date of Birth of prisoner", example = "1970-03-15", position = 9)
    @NotNull
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Age of prisoner", notes = "Full Details Only", position = 10)
    private Integer age;

    @ApiModelProperty(required = true, value = "Indicates that the person is currently in prison", position = 11)
    @NotNull
    private boolean activeFlag;

    @ApiModelProperty(value = "Image Id Ref of prisoner")
    private Long facialImageId;

    @ApiModelProperty(value = "Identifier of agency to which the prisoner is associated.")
    private String agencyId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that prisoner is assigned to.")
    private Long assignedLivingUnitId;

    @ApiModelProperty(value = "Religion of the prisoner")
    private String religion;

    @ApiModelProperty(value = "Preferred spoken language")
    private String language;

    @ApiModelProperty(value = "Interpreter required")
    private Boolean interpreterRequired;

    @ApiModelProperty(value = "Preferred written language")
    private String writtenLanguage;

    @ApiModelProperty(value = "List of Alerts")
    private List<String> alertsCodes;

    @ApiModelProperty(value = "number of active alerts", notes = "Full Details Only")
    private Long activeAlertCount;

    @ApiModelProperty(value = "number of inactive alerts", notes = "Full Details Only")
    private Long inactiveAlertCount;

    @ApiModelProperty(value = "List of alert details")
    private List<Alert> alerts;

    @ApiModelProperty(value = "Cell or location of the prisoner")
    private AssignedLivingUnit assignedLivingUnit;

    @ApiModelProperty(value = "A set of physical attributes")
    private PhysicalAttributes physicalAttributes;

    @ApiModelProperty(value = "List of physical characteristics")
    private List<PhysicalCharacteristic> physicalCharacteristics;

    @ApiModelProperty(value = "List of profile information")
    private List<ProfileInformation> profileInformation;

    @ApiModelProperty(value = "List of physical marks")
    private List<PhysicalMark> physicalMarks;

    @ApiModelProperty(value = "List of assessments")
    private List<Assessment> assessments;

    @ApiModelProperty(value = "CSRA (Latest assessment with cellSharing=true from list of assessments)")
    private String csra;

    @ApiModelProperty(value = "Category description (from list of assessments)")
    private String category;

    @ApiModelProperty(value = "Category code (from list of assessments)")
    private String categoryCode;

    @ApiModelProperty(value = "Place of birth", example = "WALES")
    private String birthPlace;

    @ApiModelProperty(value = "Country of birth", example = "GBR")
    private String birthCountryCode;

    @ApiModelProperty(value = "In/Out Status", required = true, example = "IN", allowableValues = "IN,OUT,TRN")
    private String inOutStatus;

    @ApiModelProperty(value = "Identifiers", notes = "Only returned when requesting extra details")
    private List<OffenderIdentifier> identifiers;

    @ApiModelProperty(value = "Personal Care Needs", notes = "Only returned when requesting extra details")
    private List<PersonalCareNeed> personalCareNeeds;

    @ApiModelProperty(value = "Sentence Detail", notes = "Only returned when requesting extra details")
    private SentenceDetail sentenceDetail;

    @ApiModelProperty(value = "Offence History", notes = "Only returned when requesting extra details")
    private List<OffenceHistoryDetail> offenceHistory;

    @ApiModelProperty(value = "Current Sentence Terms", notes = "Only returned when requesting extra details")
    private List<OffenderSentenceTerms> sentenceTerms;

    @ApiModelProperty(value = "Aliases", notes = "Only returned when requesting extra details")
    private List<Alias> aliases;

    @ApiModelProperty(value = "Status of prisoner", required = true, example = "ACTIVE IN", allowableValues = "ACTIVE IN,ACTIVE OUT")
    private String status;

    @ApiModelProperty(value = "Last movement status of the prison", example = "CRT-CA")
    private String statusReason;

    @ApiModelProperty(value = "Last Movement Type Code of prisoner", example = "TAP", allowableValues = "TAP,CRT,TRN,ADM,REL", notes = "Reference Data from MOVE_TYPE Domain")
    private String lastMovementTypeCode;

    @ApiModelProperty(value = "Last Movement Reason of prisoner", example = "CA", notes = "Reference Data from MOVE_RSN Domain")
    private String lastMovementReasonCode;

    @ApiModelProperty(value = "Legal Status", example = "REMAND", notes = "Only returned when requesting extra details")
    private LegalStatus legalStatus;

    @ApiModelProperty(value = "Recall", example = "true", notes = "Only returned when requesting extra details")
    private Boolean recall;

    @ApiModelProperty(value = "The prisoner's imprisonment status.", example="LIFE", notes = "Only returned when requesting extra details")
    private String imprisonmentStatus;

    @ApiModelProperty(value = "The prisoner's IEP Status", notes = "Only returned when requesting extra details")
    private PrivilegeSummary privilegeSummary;

    @ApiModelProperty(value = "Date prisoner was received into the prison.", example="1980-01-01")
    private LocalDate receptionDate;

    @ApiModelProperty(value = "current prison or outside with last movement information.", example="Outside - released from Leeds")
    private String locationDescription;

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
