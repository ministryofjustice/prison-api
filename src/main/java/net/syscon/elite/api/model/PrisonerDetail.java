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
import java.util.Map;

/**
 * Prisoner Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Prisoner Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrisonerDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String offenderNo;

    private String title;

    private String suffix;

    @NotBlank
    private String firstName;

    private String middleNames;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String gender;

    @NotBlank
    private String sexCode;

    private String nationalities;

    @NotBlank
    private String currentlyInPrison;

    private Long latestBookingId;

    private String latestLocationId;

    private String latestLocation;

    private String internalLocation;

    private String pncNumber;

    private String croNumber;

    private String ethnicity;

    private String birthCountry;

    private String religion;

    private String convictedStatus;

    private String imprisonmentStatus;

    private LocalDate receptionDate;

    private String maritalStatus;

    @NotBlank
    private String currentWorkingFirstName;

    @NotBlank
    private String currentWorkingLastName;

    @NotNull
    private LocalDate currentWorkingBirthDate;

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
      * The prisoner's unique offender number (aka NOMS Number in the UK).
      */
    @ApiModelProperty(required = true, value = "The prisoner's unique offender number (aka NOMS Number in the UK).")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * The prisoner's title.
      */
    @ApiModelProperty(value = "The prisoner's title.")
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
      * The prisoner's name suffix.
      */
    @ApiModelProperty(value = "The prisoner's name suffix.")
    @JsonProperty("suffix")
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    /**
      * The prisoner's first name.
      */
    @ApiModelProperty(required = true, value = "The prisoner's first name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
      * The prisoner's middle name(s).
      */
    @ApiModelProperty(value = "The prisoner's middle name(s).")
    @JsonProperty("middleNames")
    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(final String middleNames) {
        this.middleNames = middleNames;
    }

    /**
      * The prisoner's last name.
      */
    @ApiModelProperty(required = true, value = "The prisoner's last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
      * The prisoner's date of birth (in YYYY-MM-DD format).
      */
    @ApiModelProperty(required = true, value = "The prisoner's date of birth (in YYYY-MM-DD format).")
    @JsonProperty("dateOfBirth")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
      * The prisoner's sex code.
      */
    @ApiModelProperty(required = true, value = "The prisoner's gender code.")
    @JsonProperty("sexCode")
    public String getSexCode() {
        return sexCode;
    }

    public void setSexCode(final String sexCode) {
        this.sexCode = sexCode;
    }

    /**
     * The prisoner's gender.
     */
    @ApiModelProperty(required = true, value = "The prisoner's gender.")
    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    /**
      * The prisoner's nationality.
      */
    @ApiModelProperty(value = "The prisoner's nationality.")
    @JsonProperty("nationalities")
    public String getNationalities() {
        return nationalities;
    }

    public void setNationalities(final String nationalities) {
        this.nationalities = nationalities;
    }

    /**
      * Flag (Y or N) to indicate if prisoner is currently in prison.
      */
    @ApiModelProperty(required = true, value = "Flag (Y or N) to indicate if prisoner is currently in prison.")
    @JsonProperty("currentlyInPrison")
    public String getCurrentlyInPrison() {
        return currentlyInPrison;
    }

    public void setCurrentlyInPrison(final String currentlyInPrison) {
        this.currentlyInPrison = currentlyInPrison;
    }

    /**
      * ID of prisoner's latest booking.
      */
    @ApiModelProperty(value = "ID of prisoner's latest booking.")
    @JsonProperty("latestBookingId")
    public Long getLatestBookingId() {
        return latestBookingId;
    }

    public void setLatestBookingId(final Long latestBookingId) {
        this.latestBookingId = latestBookingId;
    }

    /**
      * Latest location ID of a prisoner (if in prison).
      */
    @ApiModelProperty(value = "Latest location ID of a prisoner (if in prison).")
    @JsonProperty("latestLocationId")
    public String getLatestLocationId() {
        return latestLocationId;
    }

    public void setLatestLocationId(final String latestLocationId) {
        this.latestLocationId = latestLocationId;
    }

    /**
      * Name of the prison where the prisoner resides (if in prison).
      */
    @ApiModelProperty(value = "Name of the prison where the prisoner resides (if in prison).")
    @JsonProperty("latestLocation")
    public String getLatestLocation() {
        return latestLocation;
    }

    public void setLatestLocation(final String latestLocation) {
        this.latestLocation = latestLocation;
    }

    /**
      * Name of the location where the prisoner resides (if in prison)
      */
    @ApiModelProperty(value = "Name of the location where the prisoner resides (if in prison)")
    @JsonProperty("internalLocation")
    public String getInternalLocation() {
        return internalLocation;
    }

    public void setInternalLocation(final String internalLocation) {
        this.internalLocation = internalLocation;
    }

    /**
      * The prisoner's PNC (Police National Computer) number.
      */
    @ApiModelProperty(value = "The prisoner's PNC (Police National Computer) number.")
    @JsonProperty("pncNumber")
    public String getPncNumber() {
        return pncNumber;
    }

    public void setPncNumber(final String pncNumber) {
        this.pncNumber = pncNumber;
    }

    /**
      * The prisoner's CRO (Criminal Records Office) number.
      */
    @ApiModelProperty(value = "The prisoner's CRO (Criminal Records Office) number.")
    @JsonProperty("croNumber")
    public String getCroNumber() {
        return croNumber;
    }

    public void setCroNumber(final String croNumber) {
        this.croNumber = croNumber;
    }

    /**
      * The prisoner's ethnicity.
      */
    @ApiModelProperty(value = "The prisoner's ethnicity.")
    @JsonProperty("ethnicity")
    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(final String ethnicity) {
        this.ethnicity = ethnicity;
    }

    /**
      * The prisoner's country of birth.
      */
    @ApiModelProperty(value = "The prisoner's country of birth.")
    @JsonProperty("birthCountry")
    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(final String birthCountry) {
        this.birthCountry = birthCountry;
    }

    /**
      * The prisoner's religion.
      */
    @ApiModelProperty(value = "The prisoner's religion.")
    @JsonProperty("religion")
    public String getReligion() {
        return religion;
    }

    public void setReligion(final String religion) {
        this.religion = religion;
    }

    /**
      * Status code of prisoner's latest conviction.
      */
    @ApiModelProperty(value = "Status code of prisoner's latest conviction.")
    @JsonProperty("convictedStatus")
    public String getConvictedStatus() {
        return convictedStatus;
    }

    public void setConvictedStatus(final String convictedStatus) {
        this.convictedStatus = convictedStatus;
    }

    /**
      * The prisoner's imprisonment status.
      */
    @ApiModelProperty(value = "The prisoner's imprisonment status.")
    @JsonProperty("imprisonmentStatus")
    public String getImprisonmentStatus() {
        return imprisonmentStatus;
    }

    public void setImprisonmentStatus(final String imprisonmentStatus) {
        this.imprisonmentStatus = imprisonmentStatus;
    }

    /**
      * Date prisoner was received into the prison.
      */
    @ApiModelProperty(value = "Date prisoner was received into the prison.")
    @JsonProperty("receptionDate")
    public LocalDate getReceptionDate() {
        return receptionDate;
    }

    public void setReceptionDate(final LocalDate receptionDate) {
        this.receptionDate = receptionDate;
    }

    /**
      * The prisoner's marital status.
      */
    @ApiModelProperty(value = "The prisoner's marital status.")
    @JsonProperty("maritalStatus")
    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(final String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    /**
      * The prisoner's current working first name.
      */
    @ApiModelProperty(required = true, value = "The prisoner's current working first name.")
    @JsonProperty("currentWorkingFirstName")
    public String getCurrentWorkingFirstName() {
        return currentWorkingFirstName;
    }

    public void setCurrentWorkingFirstName(final String currentWorkingFirstName) {
        this.currentWorkingFirstName = currentWorkingFirstName;
    }

    /**
      * The prisoner's current working last name.
      */
    @ApiModelProperty(required = true, value = "The prisoner's current working last name.")
    @JsonProperty("currentWorkingLastName")
    public String getCurrentWorkingLastName() {
        return currentWorkingLastName;
    }

    public void setCurrentWorkingLastName(final String currentWorkingLastName) {
        this.currentWorkingLastName = currentWorkingLastName;
    }

    /**
      * The prisoner's current working date of birth (in YYYY-MM-DD format).
      */
    @ApiModelProperty(required = true, value = "The prisoner's current working date of birth (in YYYY-MM-DD format).")
    @JsonProperty("currentWorkingBirthDate")
    public LocalDate getCurrentWorkingBirthDate() {
        return currentWorkingBirthDate;
    }

    public void setCurrentWorkingBirthDate(final LocalDate currentWorkingBirthDate) {
        this.currentWorkingBirthDate = currentWorkingBirthDate;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class PrisonerDetail {\n");
        
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  title: ").append(title).append("\n");
        sb.append("  suffix: ").append(suffix).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleNames: ").append(middleNames).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  gender: ").append(gender).append("\n");
        sb.append("  nationalities: ").append(nationalities).append("\n");
        sb.append("  currentlyInPrison: ").append(currentlyInPrison).append("\n");
        sb.append("  latestBookingId: ").append(latestBookingId).append("\n");
        sb.append("  latestLocationId: ").append(latestLocationId).append("\n");
        sb.append("  latestLocation: ").append(latestLocation).append("\n");
        sb.append("  internalLocation: ").append(internalLocation).append("\n");
        sb.append("  pncNumber: ").append(pncNumber).append("\n");
        sb.append("  croNumber: ").append(croNumber).append("\n");
        sb.append("  ethnicity: ").append(ethnicity).append("\n");
        sb.append("  birthCountry: ").append(birthCountry).append("\n");
        sb.append("  religion: ").append(religion).append("\n");
        sb.append("  convictedStatus: ").append(convictedStatus).append("\n");
        sb.append("  imprisonmentStatus: ").append(imprisonmentStatus).append("\n");
        sb.append("  receptionDate: ").append(receptionDate).append("\n");
        sb.append("  maritalStatus: ").append(maritalStatus).append("\n");
        sb.append("  currentWorkingFirstName: ").append(currentWorkingFirstName).append("\n");
        sb.append("  currentWorkingLastName: ").append(currentWorkingLastName).append("\n");
        sb.append("  currentWorkingBirthDate: ").append(currentWorkingBirthDate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
