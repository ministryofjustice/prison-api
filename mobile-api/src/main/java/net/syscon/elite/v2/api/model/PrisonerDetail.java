package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonDeserialize(
    as = PrisonerDetailImpl.class
)
public interface PrisonerDetail {
  Map<String, Object> getAdditionalProperties();

  void setAdditionalProperties(Map<String, Object> additionalProperties);

  String getNomsId();

  void setNomsId(String nomsId);

  String getFirstName();

  void setFirstName(String firstName);

  String getMiddleNames();

  void setMiddleNames(String middleNames);

  String getLastName();

  void setLastName(String lastName);

  Date getDateOfBirth();

  void setDateOfBirth(Date dateOfBirth);

  String getGender();

  void setGender(String gender);

  List<String> getNationalities();

  void setNationalities(List<String> nationalities);

  String getPncNumber();

  void setPncNumber(String pncNumber);

  String getCroNumber();

  void setCroNumber(String croNumber);

  String getParoleNumbers();

  void setParoleNumbers(String paroleNumbers);

  String getEthnicity();

  void setEthnicity(String ethnicity);

  String getBirthCountry();

  void setBirthCountry(String birthCountry);

  String getReligion();

  void setReligion(String religion);

  Date getReceptionDate();

  void setReceptionDate(Date receptionDate);

  String getMaritalStatus();

  void setMaritalStatus(String maritalStatus);
}
