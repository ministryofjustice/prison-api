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
import java.util.HashMap;
import java.util.Map;

/**
 * Location Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Location {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long locationId;

    @NotBlank
    private String locationType;

    @NotBlank
    private String description;

    private String locationUsage;

    @NotBlank
    private String agencyId;

    private Long parentLocationId;

    private Integer currentOccupancy;

    private String locationPrefix;

    private Integer operationalCapacity;

    private String userDescription;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Location identifier.
      */
    @ApiModelProperty(required = true, value = "Location identifier.")
    @JsonProperty("locationId")
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
      * Location type.
      */
    @ApiModelProperty(required = true, value = "Location type.")
    @JsonProperty("locationType")
    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    /**
      * Location description.
      */
    @ApiModelProperty(required = true, value = "Location description.")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
      * What events this room can be used for.
      */
    @ApiModelProperty(value = "What events this room can be used for.")
    @JsonProperty("locationUsage")
    public String getLocationUsage() {
        return locationUsage;
    }

    public void setLocationUsage(String locationUsage) {
        this.locationUsage = locationUsage;
    }

    /**
      * Identifier of Agency this location is associated with.
      */
    @ApiModelProperty(required = true, value = "Identifier of Agency this location is associated with.")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    /**
      * Identifier of this location's parent location.
      */
    @ApiModelProperty(value = "Identifier of this location's parent location.")
    @JsonProperty("parentLocationId")
    public Long getParentLocationId() {
        return parentLocationId;
    }

    public void setParentLocationId(Long parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    /**
      * Current occupancy of location.
      */
    @ApiModelProperty(value = "Current occupancy of location.")
    @JsonProperty("currentOccupancy")
    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    /**
      * Location prefix. Defines search prefix that will constrain search to this location and its subordinate locations.
      */
    @ApiModelProperty(value = "Location prefix. Defines search prefix that will constrain search to this location and its subordinate locations.")
    @JsonProperty("locationPrefix")
    public String getLocationPrefix() {
        return locationPrefix;
    }

    public void setLocationPrefix(String locationPrefix) {
        this.locationPrefix = locationPrefix;
    }

    /**
      * Operational capacity of the location.
      */
    @ApiModelProperty(value = "Operational capacity of the location.")
    @JsonProperty("operationalCapacity")
    public Integer getOperationalCapacity() {
        return operationalCapacity;
    }

    public void setOperationalCapacity(Integer operationalCapacity) {
        this.operationalCapacity = operationalCapacity;
    }

    /**
      * User-friendly location description.
      */
    @ApiModelProperty(value = "User-friendly location description.")
    @JsonProperty("userDescription")
    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class Location {\n");
        
        sb.append("  locationId: ").append(locationId).append("\n");
        sb.append("  locationType: ").append(locationType).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  locationUsage: ").append(locationUsage).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  parentLocationId: ").append(parentLocationId).append("\n");
        sb.append("  currentOccupancy: ").append(currentOccupancy).append("\n");
        sb.append("  locationPrefix: ").append(locationPrefix).append("\n");
        sb.append("  operationalCapacity: ").append(operationalCapacity).append("\n");
        sb.append("  userDescription: ").append(userDescription).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
