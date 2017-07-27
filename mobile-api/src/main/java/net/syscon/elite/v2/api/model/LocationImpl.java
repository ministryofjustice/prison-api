package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "locationId",
    "agencyId",
    "locationType",
    "description",
    "parentLocationId",
    "operationalCapacity",
    "currentOccupancy",
    "livingUnit",
    "housingUnitType",
    "activeCountStatusCode",
    "activeCountId"
})
public class LocationImpl implements Location {
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("locationId")
  private int locationId;

  @JsonProperty("agencyId")
  private String agencyId;

  @JsonProperty("locationType")
  private String locationType;

  @JsonProperty("description")
  private String description;

  @JsonProperty("parentLocationId")
  private int parentLocationId;

  @JsonProperty("operationalCapacity")
  private Integer operationalCapacity;

  @JsonProperty("currentOccupancy")
  private int currentOccupancy;

  @JsonProperty("livingUnit")
  private boolean livingUnit;

  @JsonProperty("housingUnitType")
  private String housingUnitType;

  @JsonProperty("activeCountStatusCode")
  private ActiveCountStatusCodeType activeCountStatusCode;

  @JsonProperty("activeCountId")
  private Integer activeCountId;

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  @JsonProperty("locationId")
  public int getLocationId() {
    return this.locationId;
  }

  @JsonProperty("locationId")
  public void setLocationId(int locationId) {
    this.locationId = locationId;
  }

  @JsonProperty("agencyId")
  public String getAgencyId() {
    return this.agencyId;
  }

  @JsonProperty("agencyId")
  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  @JsonProperty("locationType")
  public String getLocationType() {
    return this.locationType;
  }

  @JsonProperty("locationType")
  public void setLocationType(String locationType) {
    this.locationType = locationType;
  }

  @JsonProperty("description")
  public String getDescription() {
    return this.description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("parentLocationId")
  public int getParentLocationId() {
    return this.parentLocationId;
  }

  @JsonProperty("parentLocationId")
  public void setParentLocationId(int parentLocationId) {
    this.parentLocationId = parentLocationId;
  }

  @JsonProperty("operationalCapacity")
  public Integer getOperationalCapacity() {
    return this.operationalCapacity;
  }

  @JsonProperty("operationalCapacity")
  public void setOperationalCapacity(Integer operationalCapacity) {
    this.operationalCapacity = operationalCapacity;
  }

  @JsonProperty("currentOccupancy")
  public int getCurrentOccupancy() {
    return this.currentOccupancy;
  }

  @JsonProperty("currentOccupancy")
  public void setCurrentOccupancy(int currentOccupancy) {
    this.currentOccupancy = currentOccupancy;
  }

  @JsonProperty("livingUnit")
  public boolean getLivingUnit() {
    return this.livingUnit;
  }

  @JsonProperty("livingUnit")
  public void setLivingUnit(boolean livingUnit) {
    this.livingUnit = livingUnit;
  }

  @JsonProperty("housingUnitType")
  public String getHousingUnitType() {
    return this.housingUnitType;
  }

  @JsonProperty("housingUnitType")
  public void setHousingUnitType(String housingUnitType) {
    this.housingUnitType = housingUnitType;
  }

  @JsonProperty("activeCountStatusCode")
  public ActiveCountStatusCodeType getActiveCountStatusCode() {
    return this.activeCountStatusCode;
  }

  @JsonProperty("activeCountStatusCode")
  public void setActiveCountStatusCode(ActiveCountStatusCodeType activeCountStatusCode) {
    this.activeCountStatusCode = activeCountStatusCode;
  }

  @JsonProperty("activeCountId")
  public Integer getActiveCountId() {
    return this.activeCountId;
  }

  @JsonProperty("activeCountId")
  public void setActiveCountId(Integer activeCountId) {
    this.activeCountId = activeCountId;
  }
}
