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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offender Booking Summary
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderBooking {

    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    private String bookingNo;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private Integer age;

    @NotNull
    @Builder.Default
    private List<String> alertsCodes = new ArrayList<String>();

    @NotNull
    @Builder.Default
    private List<String> alertsDetails = new ArrayList<String>();

    @NotBlank
    private String agencyId;

    private Long assignedLivingUnitId;

    private String assignedLivingUnitDesc;

    private Long facialImageId;

    private String assignedOfficerUserId;

    private List<String> aliases;

    private String iepLevel;

    private String categoryCode;

    private String convictedStatus;

    @JsonIgnore
    private String bandCode;

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
      * Unique, numeric booking id.
      */
    @ApiModelProperty(required = true, value = "Unique, numeric booking id.")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Booking number.
      */
    @ApiModelProperty(value = "Booking number.")
    @JsonProperty("bookingNo")
    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(final String bookingNo) {
        this.bookingNo = bookingNo;
    }

    /**
      * Offender number (e.g. NOMS Number).
      */
    @ApiModelProperty(required = true, value = "Offender number (e.g. NOMS Number).")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Offender first name.
      */
    @ApiModelProperty(required = true, value = "Offender first name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
      * Offender middle name.
      */
    @ApiModelProperty(value = "Offender middle name.")
    @JsonProperty("middleName")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    /**
      * Offender last name.
      */
    @ApiModelProperty(required = true, value = "Offender last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
      * Offender date of birth.
      */
    @ApiModelProperty(required = true, value = "Offender date of birth.")
    @JsonProperty("dateOfBirth")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
      * Offender's current age.
      */
    @ApiModelProperty(required = true, value = "Offender's current age.")
    @JsonProperty("age")
    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    /**
      * List of offender's current alert types.
      */
    @ApiModelProperty(required = true, value = "List of offender's current alert types.")
    @JsonProperty("alertsCodes")
    public List<String> getAlertsCodes() {
        return alertsCodes;
    }

    public void setAlertsCodes(final List<String> alertsCodes) {
        this.alertsCodes = alertsCodes;
    }

    /**
      * List of offender's current alert codes.
      */
    @ApiModelProperty(required = true, value = "List of offender's current alert codes.")
    @JsonProperty("alertsDetails")
    public List<String> getAlertsDetails() {
        return alertsDetails;
    }

    public void setAlertsDetails(final List<String> alertsDetails) {
        this.alertsDetails = alertsDetails;
    }

    /**
      * Identifier of agency that offender is associated with.
      */
    @ApiModelProperty(required = true, value = "Identifier of agency that offender is associated with.")
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
      * Description of living unit (e.g. cell) that offender is assigned to.
      */
    @ApiModelProperty(value = "Description of living unit (e.g. cell) that offender is assigned to.")
    @JsonProperty("assignedLivingUnitDesc")
    public String getAssignedLivingUnitDesc() {
        return assignedLivingUnitDesc;
    }

    public void setAssignedLivingUnitDesc(final String assignedLivingUnitDesc) {
        this.assignedLivingUnitDesc = assignedLivingUnitDesc;
    }

    /**
      * Identifier of facial image of offender.
      */
    @ApiModelProperty(value = "Identifier of facial image of offender.")
    @JsonProperty("facialImageId")
    public Long getFacialImageId() {
        return facialImageId;
    }

    public void setFacialImageId(final Long facialImageId) {
        this.facialImageId = facialImageId;
    }

    /**
      * Identifier of officer (key worker) to which offender is assigned.
      */
    @ApiModelProperty(value = "Identifier of officer (key worker) to which offender is assigned.")
    @JsonProperty("assignedOfficerUserId")
    public String getAssignedOfficerUserId() {
        return assignedOfficerUserId;
    }

    public void setAssignedOfficerUserId(final String assignedOfficerUserId) {
        this.assignedOfficerUserId = assignedOfficerUserId;
    }

    /**
      * List of offender's alias names.
      */
    @ApiModelProperty(value = "List of offender's alias names.")
    @JsonProperty("aliases")
    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(final List<String> aliases) {
        this.aliases = aliases;
    }

    /**
      * The IEP Level of the offender (UK Only)
      */
    @ApiModelProperty(value = "The IEP Level of the offender (UK Only)")
    @JsonProperty("iepLevel")
    public String getIepLevel() {
        return iepLevel;
    }

    public void setIepLevel(final String iepLevel) {
        this.iepLevel = iepLevel;
    }

    /**
      * The Cat A/B/C/D of the offender
      */
    @ApiModelProperty(value = "The Cat A/B/C/D of the offender")
    @JsonProperty("categoryCode")
    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(final String categoryCode) {
        this.categoryCode = categoryCode;
    }

    /**
     * The convicted status of the offender one of 'Remand', 'Convicted' or null
     */
    @ApiModelProperty(value = "The convicted status of the offender calculated")
    @JsonProperty("convictedStatus")
    public String getConvictedStatus() {

        if (this.bandCode != null) {
            // Persist the value whenever the bandCode is present - as and mapping to/from JSON will lose the bandCode value
            setConvictedStatus(Integer.valueOf(bandCode) <= 8 ? "Convicted" : "Remand");
        }
        return convictedStatus;
    }

    public void setConvictedStatus(final String convictedStatus) {
        this.convictedStatus = convictedStatus;
    }

    /**
     * The bandCode is NOT returned in the JSON object - it has temporary usage in setting the convictedStatus.
     */
    public String getBandCode() {
        return bandCode;
    }

    public void setBandCode(String bandCode) {
        this.bandCode = bandCode;
    }


    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class OffenderBooking {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  bookingNo: ").append(bookingNo).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleName: ").append(middleName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  age: ").append(age).append("\n");
        sb.append("  alertsCodes: ").append(alertsCodes).append("\n");
        sb.append("  alertsDetails: ").append(alertsDetails).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  assignedLivingUnitId: ").append(assignedLivingUnitId).append("\n");
        sb.append("  assignedLivingUnitDesc: ").append(assignedLivingUnitDesc).append("\n");
        sb.append("  facialImageId: ").append(facialImageId).append("\n");
        sb.append("  assignedOfficerUserId: ").append(assignedOfficerUserId).append("\n");
        sb.append("  aliases: ").append(aliases).append("\n");
        sb.append("  iepLevel: ").append(iepLevel).append("\n");
        sb.append("  categoryCode: ").append(categoryCode).append("\n");
        sb.append("  convictedStatus: ").append(convictedStatus).append("\n");
        sb.append("  bandCode: ").append(bandCode).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
