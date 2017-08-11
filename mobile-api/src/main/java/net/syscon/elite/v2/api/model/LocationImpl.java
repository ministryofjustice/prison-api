package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "locationId",
        "locationType",
        "description",
        "agencyId",
        "parentLocationId",
        "currentOccupancy"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationImpl implements Location {
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties.clear();
        this.additionalProperties.putAll(additionalProperties);
    }

    @JsonProperty("locationId")
    public Long getLocationId() {
        return this.locationId;
    }

    @JsonProperty("locationId")
    public void setLocationId(Long locationId) {
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

    @JsonProperty("agencyId")
    public String getAgencyId() {
        return this.agencyId;
    }

    @JsonProperty("agencyId")
    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @JsonProperty("parentLocationId")
    public Long getParentLocationId() {
        return this.parentLocationId;
    }

    @JsonProperty("parentLocationId")
    public void setParentLocationId(Long parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    @JsonProperty("currentOccupancy")
    public Integer getCurrentOccupancy() {
        return this.currentOccupancy;
    }

    @JsonProperty("currentOccupancy")
    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }
}
