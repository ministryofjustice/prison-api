package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "locationId",
        "locationType",
        "description",
        "agencyId",
        "parentLocationId",
        "currentOccupancy",
        "locationPrefix"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @JsonProperty("locationId")
    private Long locationId;

    @JsonProperty("locationType")
    private String locationType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("agencyId")
    private String agencyId;

    @JsonProperty("parentLocationId")
    private Long parentLocationId;

    @JsonProperty("currentOccupancy")
    private Integer currentOccupancy;

    @JsonProperty("locationPrefix")
    private String locationPrefix;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("locationId")
    public Long getLocationId() {
        return this.locationId;
    }

    @ApiModelProperty(value = "Unique identifier for location.", required = true, position = 1)
    @NotNull
    @JsonProperty("locationId")
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    @JsonProperty("locationType")
    public String getLocationType() {
        return this.locationType;
    }

    @ApiModelProperty(value = "Location type.", required = true, position = 2)
    @NotNull
    @NotBlank
    @JsonProperty("locationType")
    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    @ApiModelProperty(value = "Location description.", required = true, position = 3)
    @NotNull
    @NotBlank
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("agencyId")
    public String getAgencyId() {
        return this.agencyId;
    }

    @ApiModelProperty(value = "Agency this location is associated with.", required = true, position = 4)
    @NotNull
    @NotBlank
    @JsonProperty("agencyId")
    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @JsonProperty("parentLocationId")
    public Long getParentLocationId() {
        return this.parentLocationId;
    }

    @ApiModelProperty(value = "Identifier of this location's parent location.", position = 5)
    @JsonProperty("parentLocationId")
    public void setParentLocationId(Long parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    @JsonProperty("currentOccupancy")
    public Integer getCurrentOccupancy() {
        return this.currentOccupancy;
    }

    @ApiModelProperty(value = "Current occupancy of location.", position = 6)
    @JsonProperty("currentOccupancy")
    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    @JsonProperty("locationPrefix")
    public String getLocationPrefix() {
        return this.locationPrefix;
    }

    @ApiModelProperty(value = "Location prefix.", position = 7, notes = "Defines search prefix that will constrain search to this location and its subordinate locations.")
    @JsonProperty("locationPrefix")
    public void setLocationPrefix(String locationPrefix) {
        this.locationPrefix = locationPrefix;
    }
}
