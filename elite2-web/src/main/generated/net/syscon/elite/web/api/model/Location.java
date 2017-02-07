
package net.syscon.elite.web.api.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


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
    private Object locationId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agencyId")
    private Object agencyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("locationType")
    private Object locationType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    private Object description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parentLocationId")
    private Object parentLocationId;
    @JsonProperty("operationalCapacity")
    private Object operationalCapacity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("currentOccupancy")
    private Object currentOccupancy;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("livingUnit")
    private Object livingUnit;
    @JsonProperty("housingUnitType")
    private Object housingUnitType;
    @JsonProperty("assignedInmates")
    private Object assignedInmates;
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
    public Object getLocationId() {
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
    public void setLocationId(Object locationId) {
        this.locationId = locationId;
    }

    public Location withLocationId(Object locationId) {
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
    public Object getAgencyId() {
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
    public void setAgencyId(Object agencyId) {
        this.agencyId = agencyId;
    }

    public Location withAgencyId(Object agencyId) {
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
    public Object getLocationType() {
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
    public void setLocationType(Object locationType) {
        this.locationType = locationType;
    }

    public Location withLocationType(Object locationType) {
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
    public Object getDescription() {
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
    public void setDescription(Object description) {
        this.description = description;
    }

    public Location withDescription(Object description) {
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
    public Object getParentLocationId() {
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
    public void setParentLocationId(Object parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    public Location withParentLocationId(Object parentLocationId) {
        this.parentLocationId = parentLocationId;
        return this;
    }

    /**
     * 
     * @return
     *     The operationalCapacity
     */
    @JsonProperty("operationalCapacity")
    public Object getOperationalCapacity() {
        return operationalCapacity;
    }

    /**
     * 
     * @param operationalCapacity
     *     The operationalCapacity
     */
    @JsonProperty("operationalCapacity")
    public void setOperationalCapacity(Object operationalCapacity) {
        this.operationalCapacity = operationalCapacity;
    }

    public Location withOperationalCapacity(Object operationalCapacity) {
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
    public Object getCurrentOccupancy() {
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
    public void setCurrentOccupancy(Object currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public Location withCurrentOccupancy(Object currentOccupancy) {
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
    public Object getLivingUnit() {
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
    public void setLivingUnit(Object livingUnit) {
        this.livingUnit = livingUnit;
    }

    public Location withLivingUnit(Object livingUnit) {
        this.livingUnit = livingUnit;
        return this;
    }

    /**
     * 
     * @return
     *     The housingUnitType
     */
    @JsonProperty("housingUnitType")
    public Object getHousingUnitType() {
        return housingUnitType;
    }

    /**
     * 
     * @param housingUnitType
     *     The housingUnitType
     */
    @JsonProperty("housingUnitType")
    public void setHousingUnitType(Object housingUnitType) {
        this.housingUnitType = housingUnitType;
    }

    public Location withHousingUnitType(Object housingUnitType) {
        this.housingUnitType = housingUnitType;
        return this;
    }

    /**
     * 
     * @return
     *     The assignedInmates
     */
    @JsonProperty("assignedInmates")
    public Object getAssignedInmates() {
        return assignedInmates;
    }

    /**
     * 
     * @param assignedInmates
     *     The assignedInmates
     */
    @JsonProperty("assignedInmates")
    public void setAssignedInmates(Object assignedInmates) {
        this.assignedInmates = assignedInmates;
    }

    public Location withAssignedInmates(Object assignedInmates) {
        this.assignedInmates = assignedInmates;
        return this;
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
