package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inmate Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Inmate Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true   )
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class InmateDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotBlank
    private String bookingNo;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String agencyId;

    private Long assignedLivingUnitId;

    @NotNull
    private boolean activeFlag;

    private String religion;

    private String language;

    private List<String> alertsCodes;

    private Long activeAlertCount;

    private Long inactiveAlertCount;

    private List<Alert> alerts;

    private AssignedLivingUnit assignedLivingUnit;

    private Long facialImageId;

    @NotNull
    private LocalDate dateOfBirth;

    private Integer age;

    private PhysicalAttributes physicalAttributes;

    private List<PhysicalCharacteristic> physicalCharacteristics;

    private List<ProfileInformation> profileInformation;

    private List<PhysicalMark> physicalMarks;

    private List<Assessment> assessments;

    private String csra;

    private String category;

    private String categoryCode;

    private Long assignedOfficerId;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender Booking Id
      */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Booking Number
      */
    @ApiModelProperty(required = true, value = "Booking Number")
    @JsonProperty("bookingNo")
    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(final String bookingNo) {
        this.bookingNo = bookingNo;
    }

    /**
      * Offender Unique Reference
      */
    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * First Name
      */
    @ApiModelProperty(required = true, value = "First Name")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
      * Middle Name(s)
      */
    @ApiModelProperty(value = "Middle Name(s)")
    @JsonProperty("middleName")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    /**
      * Last Name
      */
    @ApiModelProperty(required = true, value = "Last Name")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
      * Identifier of agency to which the offender is associated.
      */
    @ApiModelProperty(required = true, value = "Identifier of agency to which the offender is associated.")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(final String agencyId) {
        this.agencyId = agencyId;
    }

    /**
      * Identifier of living unit (e.g. cell) that offender is assigned to.
      */
    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that offender is assigned to.")
    @JsonProperty("assignedLivingUnitId")
    public Long getAssignedLivingUnitId() {
        return assignedLivingUnitId;
    }

    public void setAssignedLivingUnitId(final Long assignedLivingUnitId) {
        this.assignedLivingUnitId = assignedLivingUnitId;
    }

    /**
      * Indicates that the offender is currently in prison
      */
    @ApiModelProperty(required = true, value = "Indicates that the offender is currently in prison")
    @JsonProperty("activeFlag")
    public boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(final boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    /**
      * Religion of the prisoner
      */
    @ApiModelProperty(value = "Religion of the prisoner")
    @JsonProperty("religion")
    public String getReligion() {
        return religion;
    }

    public void setReligion(final String religion) {
        this.religion = religion;
    }

    /**
      * The prisoner's preferred spoken language.
      */
    @ApiModelProperty(value = "The prisoner's preferred spoken language.")
    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
      * List of Alerts
      */
    @ApiModelProperty(value = "List of Alerts")
    @JsonProperty("alertsCodes")
    public List<String> getAlertsCodes() {
        return alertsCodes;
    }

    public void setAlertsCodes(final List<String> alertsCodes) {
        this.alertsCodes = alertsCodes;
    }

    /**
      * number of active alerts - Full Details Only
      */
    @ApiModelProperty(value = "number of active alerts - Full Details Only")
    @JsonProperty("activeAlertCount")
    public Long getActiveAlertCount() {
        return activeAlertCount;
    }

    public void setActiveAlertCount(final Long activeAlertCount) {
        this.activeAlertCount = activeAlertCount;
    }

    /**
      * number of inactive alerts - Full Details Only
      */
    @ApiModelProperty(value = "number of inactive alerts - Full Details Only")
    @JsonProperty("inactiveAlertCount")
    public Long getInactiveAlertCount() {
        return inactiveAlertCount;
    }

    public void setInactiveAlertCount(final Long inactiveAlertCount) {
        this.inactiveAlertCount = inactiveAlertCount;
    }

    /**
      * List of alert details
      */
    @ApiModelProperty(value = "List of alert details")
    @JsonProperty("alerts")
    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(final List<Alert> alerts) {
        this.alerts = alerts;
    }

    /**
      * Where the offender is staying
      */
    @ApiModelProperty(value = "Where the offender is staying")
    @JsonProperty("assignedLivingUnit")
    public AssignedLivingUnit getAssignedLivingUnit() {
        return assignedLivingUnit;
    }

    public void setAssignedLivingUnit(final AssignedLivingUnit assignedLivingUnit) {
        this.assignedLivingUnit = assignedLivingUnit;
    }

    /**
      * Image Id Ref of Offender
      */
    @ApiModelProperty(value = "Image Id Ref of Offender")
    @JsonProperty("facialImageId")
    public Long getFacialImageId() {
        return facialImageId;
    }

    public void setFacialImageId(final Long facialImageId) {
        this.facialImageId = facialImageId;
    }

    /**
      * Date of Birth of offender
      */
    @ApiModelProperty(required = true, value = "Date of Birth of offender")
    @JsonProperty("dateOfBirth")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
      * Age of offender - Full Details Only
      */
    @ApiModelProperty(value = "Age of offender - Full Details Only")
    @JsonProperty("age")
    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    /**
      * A set of physical attributes
      */
    @ApiModelProperty(value = "A set of physical attributes")
    @JsonProperty("physicalAttributes")
    public PhysicalAttributes getPhysicalAttributes() {
        return physicalAttributes;
    }

    public void setPhysicalAttributes(final PhysicalAttributes physicalAttributes) {
        this.physicalAttributes = physicalAttributes;
    }

    /**
      * List of physical characteristics
      */
    @ApiModelProperty(value = "List of physical characteristics")
    @JsonProperty("physicalCharacteristics")
    public List<PhysicalCharacteristic> getPhysicalCharacteristics() {
        return physicalCharacteristics;
    }

    public void setPhysicalCharacteristics(final List<PhysicalCharacteristic> physicalCharacteristics) {
        this.physicalCharacteristics = physicalCharacteristics;
    }

    /**
      * List of profile information
      */
    @ApiModelProperty(value = "List of profile information")
    @JsonProperty("profileInformation")
    public List<ProfileInformation> getProfileInformation() {
        return profileInformation;
    }

    public void setProfileInformation(final List<ProfileInformation> profileInformation) {
        this.profileInformation = profileInformation;
    }

    /**
      * List of physical marks
      */
    @ApiModelProperty(value = "List of physical marks")
    @JsonProperty("physicalMarks")
    public List<PhysicalMark> getPhysicalMarks() {
        return physicalMarks;
    }

    public void setPhysicalMarks(final List<PhysicalMark> physicalMarks) {
        this.physicalMarks = physicalMarks;
    }

    /**
      * List of assessments
      */
    @ApiModelProperty(value = "List of assessments")
    @JsonProperty("assessments")
    public List<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(final List<Assessment> assessments) {
        this.assessments = assessments;
    }

    /**
      * CSRA (Latest assessment with cellSharing=true from list of assessments)
      */
    @ApiModelProperty(value = "CSRA (Latest assessment with cellSharing=true from list of assessments)")
    @JsonProperty("csra")
    public String getCsra() {
        return csra;
    }

    public void setCsra(final String csra) {
        this.csra = csra;
    }

    /**
      * Category description (from list of assessments)
      */
    @ApiModelProperty(value = "Category description (from list of assessments)")
    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    /**
      * Category code (from list of assessments)
      */
    @ApiModelProperty(value = "Category code (from list of assessments)")
    @JsonProperty("categoryCode")
    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(final String categoryCode) {
        this.categoryCode = categoryCode;
    }

    /**
      * Staff Id reference for assigned officer / keyworker
      */
    @ApiModelProperty(value = "Staff Id reference for assigned officer / keyworker")
    @JsonProperty("assignedOfficerId")
    public Long getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(final Long assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class InmateDetail {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  bookingNo: ").append(bookingNo).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleName: ").append(middleName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  assignedLivingUnitId: ").append(assignedLivingUnitId).append("\n");
        sb.append("  activeFlag: ").append(activeFlag).append("\n");
        sb.append("  religion: ").append(religion).append("\n");
        sb.append("  language: ").append(language).append("\n");
        sb.append("  alertsCodes: ").append(alertsCodes).append("\n");
        sb.append("  activeAlertCount: ").append(activeAlertCount).append("\n");
        sb.append("  inactiveAlertCount: ").append(inactiveAlertCount).append("\n");
        sb.append("  alerts: ").append(alerts).append("\n");
        sb.append("  assignedLivingUnit: ").append(assignedLivingUnit).append("\n");
        sb.append("  facialImageId: ").append(facialImageId).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  age: ").append(age).append("\n");
        sb.append("  physicalAttributes: ").append(physicalAttributes).append("\n");
        sb.append("  physicalCharacteristics: ").append(physicalCharacteristics).append("\n");
        sb.append("  profileInformation: ").append(profileInformation).append("\n");
        sb.append("  physicalMarks: ").append(physicalMarks).append("\n");
        sb.append("  assessments: ").append(assessments).append("\n");
        sb.append("  csra: ").append(csra).append("\n");
        sb.append("  category: ").append(category).append("\n");
        sb.append("  categoryCode: ").append(categoryCode).append("\n");
        sb.append("  assignedOfficerId: ").append(assignedOfficerId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
