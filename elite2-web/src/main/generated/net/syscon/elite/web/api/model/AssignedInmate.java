
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
 * Inmate Summary
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "inmateId",
    "bookingId",
    "offenderId",
    "firstName",
    "lastName",
    "alertsCodes",
    "agencyId",
    "currentLocationId",
    "assignedLivingUnitId"
})
public class AssignedInmate {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("inmateId")
    private Long inmateId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bookingId")
    private String bookingId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("offenderId")
    private String offenderId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("firstName")
    private String firstName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("alertsCodes")
    private List<String> alertsCodes = new ArrayList<String>();
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
    @JsonProperty("currentLocationId")
    private Long currentLocationId;
    @JsonProperty("assignedLivingUnitId")
    private Long assignedLivingUnitId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public AssignedInmate() {
    }

    /**
     * 
     * @param firstName
     * @param lastName
     * @param alertsCodes
     * @param inmateId
     * @param agencyId
     * @param assignedLivingUnitId
     * @param offenderId
     * @param currentLocationId
     * @param bookingId
     */
    public AssignedInmate(Long inmateId, String bookingId, String offenderId, String firstName, String lastName, List<String> alertsCodes, String agencyId, Long currentLocationId, Long assignedLivingUnitId) {
        this.inmateId = inmateId;
        this.bookingId = bookingId;
        this.offenderId = offenderId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.alertsCodes = alertsCodes;
        this.agencyId = agencyId;
        this.currentLocationId = currentLocationId;
        this.assignedLivingUnitId = assignedLivingUnitId;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The inmateId
     */
    @JsonProperty("inmateId")
    public Long getInmateId() {
        return inmateId;
    }

    /**
     * 
     * (Required)
     * 
     * @param inmateId
     *     The inmateId
     */
    @JsonProperty("inmateId")
    public void setInmateId(Long inmateId) {
        this.inmateId = inmateId;
    }

    public AssignedInmate withInmateId(Long inmateId) {
        this.inmateId = inmateId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The bookingId
     */
    @JsonProperty("bookingId")
    public String getBookingId() {
        return bookingId;
    }

    /**
     * 
     * (Required)
     * 
     * @param bookingId
     *     The bookingId
     */
    @JsonProperty("bookingId")
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public AssignedInmate withBookingId(String bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The offenderId
     */
    @JsonProperty("offenderId")
    public String getOffenderId() {
        return offenderId;
    }

    /**
     * 
     * (Required)
     * 
     * @param offenderId
     *     The offenderId
     */
    @JsonProperty("offenderId")
    public void setOffenderId(String offenderId) {
        this.offenderId = offenderId;
    }

    public AssignedInmate withOffenderId(String offenderId) {
        this.offenderId = offenderId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The firstName
     */
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    /**
     * 
     * (Required)
     * 
     * @param firstName
     *     The firstName
     */
    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public AssignedInmate withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The lastName
     */
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * 
     * (Required)
     * 
     * @param lastName
     *     The lastName
     */
    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public AssignedInmate withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * 
     * @return
     *     The alertsCodes
     */
    @JsonProperty("alertsCodes")
    public List<String> getAlertsCodes() {
        return alertsCodes;
    }

    /**
     * 
     * @param alertsCodes
     *     The alertsCodes
     */
    @JsonProperty("alertsCodes")
    public void setAlertsCodes(List<String> alertsCodes) {
        this.alertsCodes = alertsCodes;
    }

    public AssignedInmate withAlertsCodes(List<String> alertsCodes) {
        this.alertsCodes = alertsCodes;
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

    public AssignedInmate withAgencyId(String agencyId) {
        this.agencyId = agencyId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The currentLocationId
     */
    @JsonProperty("currentLocationId")
    public Long getCurrentLocationId() {
        return currentLocationId;
    }

    /**
     * 
     * (Required)
     * 
     * @param currentLocationId
     *     The currentLocationId
     */
    @JsonProperty("currentLocationId")
    public void setCurrentLocationId(Long currentLocationId) {
        this.currentLocationId = currentLocationId;
    }

    public AssignedInmate withCurrentLocationId(Long currentLocationId) {
        this.currentLocationId = currentLocationId;
        return this;
    }

    /**
     * 
     * @return
     *     The assignedLivingUnitId
     */
    @JsonProperty("assignedLivingUnitId")
    public Long getAssignedLivingUnitId() {
        return assignedLivingUnitId;
    }

    /**
     * 
     * @param assignedLivingUnitId
     *     The assignedLivingUnitId
     */
    @JsonProperty("assignedLivingUnitId")
    public void setAssignedLivingUnitId(Long assignedLivingUnitId) {
        this.assignedLivingUnitId = assignedLivingUnitId;
    }

    public AssignedInmate withAssignedLivingUnitId(Long assignedLivingUnitId) {
        this.assignedLivingUnitId = assignedLivingUnitId;
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

    public AssignedInmate withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(inmateId).append(bookingId).append(offenderId).append(firstName).append(lastName).append(alertsCodes).append(agencyId).append(currentLocationId).append(assignedLivingUnitId).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AssignedInmate) == false) {
            return false;
        }
        AssignedInmate rhs = ((AssignedInmate) other);
        return new EqualsBuilder().append(inmateId, rhs.inmateId).append(bookingId, rhs.bookingId).append(offenderId, rhs.offenderId).append(firstName, rhs.firstName).append(lastName, rhs.lastName).append(alertsCodes, rhs.alertsCodes).append(agencyId, rhs.agencyId).append(currentLocationId, rhs.currentLocationId).append(assignedLivingUnitId, rhs.assignedLivingUnitId).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
