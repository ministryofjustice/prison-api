package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.Map;

@JsonDeserialize(
    as = LocationImpl.class
)
public interface Location {
  Map<String, Object> getAdditionalProperties();

  void setAdditionalProperties(Map<String, Object> additionalProperties);

  int getLocationId();

  void setLocationId(int locationId);

  String getAgencyId();

  void setAgencyId(String agencyId);

  String getLocationType();

  void setLocationType(String locationType);

  String getDescription();

  void setDescription(String description);

  int getParentLocationId();

  void setParentLocationId(int parentLocationId);

  Integer getOperationalCapacity();

  void setOperationalCapacity(Integer operationalCapacity);

  int getCurrentOccupancy();

  void setCurrentOccupancy(int currentOccupancy);

  boolean getLivingUnit();

  void setLivingUnit(boolean livingUnit);

  String getHousingUnitType();

  void setHousingUnitType(String housingUnitType);

  ActiveCountStatusCodeType getActiveCountStatusCode();

  void setActiveCountStatusCode(ActiveCountStatusCodeType activeCountStatusCode);

  Integer getActiveCountId();

  void setActiveCountId(Integer activeCountId);

  enum ActiveCountStatusCodeType {
    @JsonProperty("I")
    I("I"),

    @JsonProperty("R")
    R("R"),

    @JsonProperty("C")
    C("C");

    private String name;

    ActiveCountStatusCodeType(String name) {
      this.name = name;
    }
  }
}
