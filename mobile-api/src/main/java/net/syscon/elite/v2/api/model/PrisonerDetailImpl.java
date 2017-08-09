package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "nomsId",
    "firstName",
    "middleNames",
    "lastName",
    "dateOfBirth",
    "gender",
    "nationalities",
    "pncNumber",
    "croNumber",
    "paroleNumbers",
    "ethnicity",
    "birthCountry",
    "religion",
    "receptionDate",
    "maritalStatus"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrisonerDetailImpl implements PrisonerDetail {
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("nomsId")
  private String nomsId;

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
  private List<String> nationalities;

  @JsonProperty("pncNumber")
  private String pncNumber;

  @JsonProperty("croNumber")
  private String croNumber;

  @JsonProperty("paroleNumbers")
  private String paroleNumbers;

  @JsonProperty("ethnicity")
  private String ethnicity;

  @JsonProperty("birthCountry")
  private String birthCountry;

  @JsonProperty("religion")
  private String religion;

  @JsonProperty("receptionDate")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd"
  )
  private Date receptionDate;

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

  @JsonProperty("nomsId")
  public String getNomsId() {
    return this.nomsId;
  }

  @JsonProperty("nomsId")
  public void setNomsId(String nomsId) {
    this.nomsId = nomsId;
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
  public List<String> getNationalities() {
    return this.nationalities;
  }

  @JsonProperty("nationalities")
  public void setNationalities(List<String> nationalities) {
    this.nationalities = nationalities;
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

  @JsonProperty("paroleNumbers")
  public String getParoleNumbers() {
    return this.paroleNumbers;
  }

  @JsonProperty("paroleNumbers")
  public void setParoleNumbers(String paroleNumbers) {
    this.paroleNumbers = paroleNumbers;
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

  @JsonProperty("receptionDate")
  public Date getReceptionDate() {
    return this.receptionDate;
  }

  @JsonProperty("receptionDate")
  public void setReceptionDate(Date receptionDate) {
    this.receptionDate = receptionDate;
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
