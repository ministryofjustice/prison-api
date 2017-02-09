
package net.syscon.elite.web.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Location
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
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
    "assignedInmates"
})
public class Location {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("locationId")
    private String locationId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agencyId")
    private String agencyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("locationType")
    private String locationType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parentLocationId")
    private String parentLocationId;
    @JsonProperty("operationalCapacity")
    private Double operationalCapacity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("currentOccupancy")
    private Double currentOccupancy;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("livingUnit")
    private Boolean livingUnit;
    @JsonProperty("housingUnitType")
    private String housingUnitType;
    @JsonProperty("assignedInmates")
    private List<InmateSummary> assignedInmates = new ArrayList<InmateSummary>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The locationId
     */
    @JsonProperty("locationId")
    public String getLocationId() {
        return locationId;
    }

    /**
     * 
     * (Required)
     * 
     * @param locationId
     *     The locationId
     */
    @JsonProperty("locationId")
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Location withLocationId(String locationId) {
        this.locationId = locationId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The agencyId
     */
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    /**
     * 
     * (Required)
     * 
     * @param agencyId
     *     The agencyId
     */
    @JsonProperty("agencyId")
    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public Location withAgencyId(String agencyId) {
        this.agencyId = agencyId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The locationType
     */
    @JsonProperty("locationType")
    public String getLocationType() {
        return locationType;
    }

    /**
     * 
     * (Required)
     * 
     * @param locationType
     *     The locationType
     */
    @JsonProperty("locationType")
    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Location withLocationType(String locationType) {
        this.locationType = locationType;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * (Required)
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public Location withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The parentLocationId
     */
    @JsonProperty("parentLocationId")
    public String getParentLocationId() {
        return parentLocationId;
    }

    /**
     * 
     * (Required)
     * 
     * @param parentLocationId
     *     The parentLocationId
     */
    @JsonProperty("parentLocationId")
    public void setParentLocationId(String parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    public Location withParentLocationId(String parentLocationId) {
        this.parentLocationId = parentLocationId;
        return this;
    }

    /**
     * 
     * @return
     *     The operationalCapacity
     */
    @JsonProperty("operationalCapacity")
    public Double getOperationalCapacity() {
        return operationalCapacity;
    }

    /**
     * 
     * @param operationalCapacity
     *     The operationalCapacity
     */
    @JsonProperty("operationalCapacity")
    public void setOperationalCapacity(Double operationalCapacity) {
        this.operationalCapacity = operationalCapacity;
    }

    public Location withOperationalCapacity(Double operationalCapacity) {
        this.operationalCapacity = operationalCapacity;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The currentOccupancy
     */
    @JsonProperty("currentOccupancy")
    public Double getCurrentOccupancy() {
        return currentOccupancy;
    }

    /**
     * 
     * (Required)
     * 
     * @param currentOccupancy
     *     The currentOccupancy
     */
    @JsonProperty("currentOccupancy")
    public void setCurrentOccupancy(Double currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public Location withCurrentOccupancy(Double currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The livingUnit
     */
    @JsonProperty("livingUnit")
    public Boolean getLivingUnit() {
        return livingUnit;
    }

    /**
     * 
     * (Required)
     * 
     * @param livingUnit
     *     The livingUnit
     */
    @JsonProperty("livingUnit")
    public void setLivingUnit(Boolean livingUnit) {
        this.livingUnit = livingUnit;
    }

    public Location withLivingUnit(Boolean livingUnit) {
        this.livingUnit = livingUnit;
        return this;
    }

    /**
     * 
     * @return
     *     The housingUnitType
     */
    @JsonProperty("housingUnitType")
    public String getHousingUnitType() {
        return housingUnitType;
    }

    /**
     * 
     * @param housingUnitType
     *     The housingUnitType
     */
    @JsonProperty("housingUnitType")
    public void setHousingUnitType(String housingUnitType) {
        this.housingUnitType = housingUnitType;
    }

    public Location withHousingUnitType(String housingUnitType) {
        this.housingUnitType = housingUnitType;
        return this;
    }

    /**
     * 
     * @return
     *     The assignedInmates
     */
    @JsonProperty("assignedInmates")
    public List<InmateSummary> getAssignedInmates() {
        return assignedInmates;
    }

    /**
     * 
     * @param assignedInmates
     *     The assignedInmates
     */
    @JsonProperty("assignedInmates")
    public void setAssignedInmates(List<InmateSummary> assignedInmates) {
        this.assignedInmates = assignedInmates;
    }

    public Location withAssignedInmates(List<InmateSummary> assignedInmates) {
        this.assignedInmates = assignedInmates;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Location withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
