package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "locationId",
        "locationType",
        "description",
        "parentLocationId",
        "currentOccupancy"
})
public class LocationImpl implements Location {
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("locationId")
    private int locationId;

    @JsonProperty("locationType")
    private String locationType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("parentLocationId")
    private int parentLocationId;

    @JsonProperty("currentOccupancy")
    private int currentOccupancy;

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

    @JsonProperty("currentOccupancy")
    public int getCurrentOccupancy() {
        return this.currentOccupancy;
    }

    @JsonProperty("currentOccupancy")
    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }
}
