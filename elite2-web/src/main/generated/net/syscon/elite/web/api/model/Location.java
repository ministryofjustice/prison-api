
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    private Long locationId;
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
    private Long parentLocationId;
    @JsonProperty("operationalCapacity")
    private Long operationalCapacity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("currentOccupancy")
    private Long currentOccupancy;
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
    private List<AssignedInmate> assignedInmates = new ArrayList<AssignedInmate>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Location() {
    }

    /**
     * 
     * @param housingUnitType
     * @param parentLocationId
     * @param livingUnit
     * @param assignedInmates
     * @param locationId
     * @param operationalCapacity
     * @param locationType
     * @param description
     * @param agencyId
     * @param currentOccupancy
     */
    public Location(Long locationId, String agencyId, String locationType, String description, Long parentLocationId, Long operationalCapacity, Long currentOccupancy, Boolean livingUnit, String housingUnitType, List<AssignedInmate> assignedInmates) {
        this.locationId = locationId;
        this.agencyId = agencyId;
        this.locationType = locationType;
        this.description = description;
        this.parentLocationId = parentLocationId;
        this.operationalCapacity = operationalCapacity;
        this.currentOccupancy = currentOccupancy;
        this.livingUnit = livingUnit;
        this.housingUnitType = housingUnitType;
        this.assignedInmates = assignedInmates;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The locationId
     */
    @JsonProperty("locationId")
    public Long getLocationId() {
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
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Location withLocationId(Long locationId) {
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
    public Long getParentLocationId() {
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
    public void setParentLocationId(Long parentLocationId) {
        this.parentLocationId = parentLocationId;
    }

    public Location withParentLocationId(Long parentLocationId) {
        this.parentLocationId = parentLocationId;
        return this;
    }

    /**
     * 
     * @return
     *     The operationalCapacity
     */
    @JsonProperty("operationalCapacity")
    public Long getOperationalCapacity() {
        return operationalCapacity;
    }

    /**
     * 
     * @param operationalCapacity
     *     The operationalCapacity
     */
    @JsonProperty("operationalCapacity")
    public void setOperationalCapacity(Long operationalCapacity) {
        this.operationalCapacity = operationalCapacity;
    }

    public Location withOperationalCapacity(Long operationalCapacity) {
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
    public Long getCurrentOccupancy() {
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
    public void setCurrentOccupancy(Long currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public Location withCurrentOccupancy(Long currentOccupancy) {
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
    public List<AssignedInmate> getAssignedInmates() {
        return assignedInmates;
    }

    /**
     * 
     * @param assignedInmates
     *     The assignedInmates
     */
    @JsonProperty("assignedInmates")
    public void setAssignedInmates(List<AssignedInmate> assignedInmates) {
        this.assignedInmates = assignedInmates;
    }

    public Location withAssignedInmates(List<AssignedInmate> assignedInmates) {
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(locationId).append(agencyId).append(locationType).append(description).append(parentLocationId).append(operationalCapacity).append(currentOccupancy).append(livingUnit).append(housingUnitType).append(assignedInmates).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Location) == false) {
            return false;
        }
        Location rhs = ((Location) other);
        return new EqualsBuilder().append(locationId, rhs.locationId).append(agencyId, rhs.agencyId).append(locationType, rhs.locationType).append(description, rhs.description).append(parentLocationId, rhs.parentLocationId).append(operationalCapacity, rhs.operationalCapacity).append(currentOccupancy, rhs.currentOccupancy).append(livingUnit, rhs.livingUnit).append(housingUnitType, rhs.housingUnitType).append(assignedInmates, rhs.assignedInmates).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
