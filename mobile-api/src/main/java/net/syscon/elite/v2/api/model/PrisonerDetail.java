package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "offenderNo",
        "title",
        "suffix",
        "firstName",
        "middleNames",
        "lastName",
        "dateOfBirth",
        "gender",
        "nationalities",
        "currentlyInPrison",
        "latestLocationId",
        "latestLocation",
        "pncNumber",
        "croNumber",
        "ethnicity",
        "birthCountry",
        "religion",
        "convictedStatus",
        "imprisonmentStatus",
        "receptionDate",
        "releaseDate",
        "paroleNumbers",
        "maritalStatus"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrisonerDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("offenderNo")
    private String offenderNo;

    @JsonProperty("title")
    private String title;

    @JsonProperty("suffix")
    private String suffix;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleNames")
    private String middleNames;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("dateOfBirth")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd"
    )
    private Date dateOfBirth;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("nationalities")
    private String nationalities;

    @JsonProperty("currentlyInPrison")
    private String currentlyInPrison;

    @JsonProperty("latestLocationId")
    private String latestLocationId;

    @JsonProperty("latestLocation")
    private String latestLocation;

    @JsonProperty("pncNumber")
    private String pncNumber;

    @JsonProperty("croNumber")
    private String croNumber;

    @JsonProperty("ethnicity")
    private String ethnicity;

    @JsonProperty("birthCountry")
    private String birthCountry;

    @JsonProperty("religion")
    private String religion;

    @JsonProperty("convictedStatus")
    private String convictedStatus;

    @JsonProperty("imprisonmentStatus")
    private String imprisonmentStatus;

    @JsonProperty("receptionDate")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd"
    )
    private Date receptionDate;

    @JsonProperty("releaseDate")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd"
    )
    private Date releaseDate;

    @JsonProperty("paroleNumbers")
    private String paroleNumbers;

    @JsonProperty("maritalStatus")
    private String maritalStatus;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return this.offenderNo;
    }

    @JsonProperty("offenderNo")
    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    @JsonProperty("title")
    public String getTitle() {
        return this.title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("suffix")
    public String getSuffix() {
        return this.suffix;
    }

    @JsonProperty("suffix")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return this.firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("middleNames")
    public String getMiddleNames() {
        return this.middleNames;
    }

    @JsonProperty("middleNames")
    public void setMiddleNames(String middleNames) {
        this.middleNames = middleNames;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return this.lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("dateOfBirth")
    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    @JsonProperty("dateOfBirth")
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @JsonProperty("gender")
    public String getGender() {
        return this.gender;
    }

    @JsonProperty("gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

    @JsonProperty("nationalities")
    public String getNationalities() {
        return this.nationalities;
    }

    @JsonProperty("nationalities")
    public void setNationalities(String nationalities) {
        this.nationalities = nationalities;
    }

    @JsonProperty("currentlyInPrison")
    public String getCurrentlyInPrison() {
        return this.currentlyInPrison;
    }

    @JsonProperty("currentlyInPrison")
    public void setCurrentlyInPrison(String currentlyInPrison) {
        this.currentlyInPrison = currentlyInPrison;
    }

    @JsonProperty("latestLocationId")
    public String getLatestLocationId() {
        return this.latestLocationId;
    }

    @JsonProperty("latestLocationId")
    public void setLatestLocationId(String latestLocationId) {
        this.latestLocationId = latestLocationId;
    }

    @JsonProperty("latestLocation")
    public String getLatestLocation() {
        return this.latestLocation;
    }

    @JsonProperty("latestLocation")
    public void setLatestLocation(String latestLocation) {
        this.latestLocation = latestLocation;
    }

    @JsonProperty("pncNumber")
    public String getPncNumber() {
        return this.pncNumber;
    }

    @JsonProperty("pncNumber")
    public void setPncNumber(String pncNumber) {
        this.pncNumber = pncNumber;
    }

    @JsonProperty("croNumber")
    public String getCroNumber() {
        return this.croNumber;
    }

    @JsonProperty("croNumber")
    public void setCroNumber(String croNumber) {
        this.croNumber = croNumber;
    }

    @JsonProperty("ethnicity")
    public String getEthnicity() {
        return this.ethnicity;
    }

    @JsonProperty("ethnicity")
    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    @JsonProperty("birthCountry")
    public String getBirthCountry() {
        return this.birthCountry;
    }

    @JsonProperty("birthCountry")
    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    @JsonProperty("religion")
    public String getReligion() {
        return this.religion;
    }

    @JsonProperty("religion")
    public void setReligion(String religion) {
        this.religion = religion;
    }

    @JsonProperty("convictedStatus")
    public String getConvictedStatus() {
        return this.convictedStatus;
    }

    @JsonProperty("convictedStatus")
    public void setConvictedStatus(String convictedStatus) {
        this.convictedStatus = convictedStatus;
    }

    @JsonProperty("imprisonmentStatus")
    public String getImprisonmentStatus() {
        return this.imprisonmentStatus;
    }

    @JsonProperty("imprisonmentStatus")
    public void setImprisonmentStatus(String imprisonmentStatus) {
        this.imprisonmentStatus = imprisonmentStatus;
    }

    @JsonProperty("receptionDate")
    public Date getReceptionDate() {
        return this.receptionDate;
    }

    @JsonProperty("receptionDate")
    public void setReceptionDate(Date receptionDate) {
        this.receptionDate = receptionDate;
    }

    @JsonProperty("releaseDate")
    public Date getReleaseDate() {
        return this.releaseDate;
    }

    @JsonProperty("releaseDate")
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    @JsonProperty("paroleNumbers")
    public String getParoleNumbers() {
        return this.paroleNumbers;
    }

    @JsonProperty("paroleNumbers")
    public void setParoleNumbers(String paroleNumbers) {
        this.paroleNumbers = paroleNumbers;
    }

    @JsonProperty("maritalStatus")
    public String getMaritalStatus() {
        return this.maritalStatus;
    }

    @JsonProperty("maritalStatus")
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
}
