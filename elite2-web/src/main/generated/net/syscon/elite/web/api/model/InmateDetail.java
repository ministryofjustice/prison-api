
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
 * Inmate Detail
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
    "middleName",
    "lastName",
    "alertsCodes",
    "agencyId",
    "currentLocationId",
    "assignedLivingUnitId",
    "dateOfBirth",
    "age",
    "physicalAttributes",
    "physicalCharacteristics",
    "physicalMarks"
})
public class InmateDetail {

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
    @JsonProperty("middleName")
    private String middleName;
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
    private Double currentLocationId;
    @JsonProperty("assignedLivingUnitId")
    private Double assignedLivingUnitId;
    @JsonProperty("dateOfBirth")
    private String dateOfBirth;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("age")
    private Long age;
    /**
     * 
     */
    @JsonProperty("physicalAttributes")
    private PhysicalAttributes physicalAttributes;
    @JsonProperty("physicalCharacteristics")
    private List<PhysicalCharacteristic> physicalCharacteristics = new ArrayList<PhysicalCharacteristic>();
    @JsonProperty("physicalMarks")
    private List<PhysicalMark> physicalMarks = new ArrayList<PhysicalMark>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public InmateDetail() {
    }

    /**
     * 
     * @param lastName
     * @param physicalMarks
     * @param physicalCharacteristics
     * @param agencyId
     * @param assignedLivingUnitId
     * @param dateOfBirth
     * @param physicalAttributes
     * @param bookingId
     * @param firstName
     * @param alertsCodes
     * @param inmateId
     * @param middleName
     * @param offenderId
     * @param currentLocationId
     * @param age
     */
    public InmateDetail(Long inmateId, String bookingId, String offenderId, String firstName, String middleName, String lastName, List<String> alertsCodes, String agencyId, Double currentLocationId, Double assignedLivingUnitId, String dateOfBirth, Long age, PhysicalAttributes physicalAttributes, List<PhysicalCharacteristic> physicalCharacteristics, List<PhysicalMark> physicalMarks) {
        this.inmateId = inmateId;
        this.bookingId = bookingId;
        this.offenderId = offenderId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.alertsCodes = alertsCodes;
        this.agencyId = agencyId;
        this.currentLocationId = currentLocationId;
        this.assignedLivingUnitId = assignedLivingUnitId;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.physicalAttributes = physicalAttributes;
        this.physicalCharacteristics = physicalCharacteristics;
        this.physicalMarks = physicalMarks;
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

    public InmateDetail withInmateId(Long inmateId) {
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

    public InmateDetail withBookingId(String bookingId) {
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

    public InmateDetail withOffenderId(String offenderId) {
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

    public InmateDetail withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The middleName
     */
    @JsonProperty("middleName")
    public String getMiddleName() {
        return middleName;
    }

    /**
     * 
     * (Required)
     * 
     * @param middleName
     *     The middleName
     */
    @JsonProperty("middleName")
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public InmateDetail withMiddleName(String middleName) {
        this.middleName = middleName;
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

    public InmateDetail withLastName(String lastName) {
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

    public InmateDetail withAlertsCodes(List<String> alertsCodes) {
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

    public InmateDetail withAgencyId(String agencyId) {
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
    public Double getCurrentLocationId() {
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
    public void setCurrentLocationId(Double currentLocationId) {
        this.currentLocationId = currentLocationId;
    }

    public InmateDetail withCurrentLocationId(Double currentLocationId) {
        this.currentLocationId = currentLocationId;
        return this;
    }

    /**
     * 
     * @return
     *     The assignedLivingUnitId
     */
    @JsonProperty("assignedLivingUnitId")
    public Double getAssignedLivingUnitId() {
        return assignedLivingUnitId;
    }

    /**
     * 
     * @param assignedLivingUnitId
     *     The assignedLivingUnitId
     */
    @JsonProperty("assignedLivingUnitId")
    public void setAssignedLivingUnitId(Double assignedLivingUnitId) {
        this.assignedLivingUnitId = assignedLivingUnitId;
    }

    public InmateDetail withAssignedLivingUnitId(Double assignedLivingUnitId) {
        this.assignedLivingUnitId = assignedLivingUnitId;
        return this;
    }

    /**
     * 
     * @return
     *     The dateOfBirth
     */
    @JsonProperty("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * 
     * @param dateOfBirth
     *     The dateOfBirth
     */
    @JsonProperty("dateOfBirth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public InmateDetail withDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The age
     */
    @JsonProperty("age")
    public Long getAge() {
        return age;
    }

    /**
     * 
     * (Required)
     * 
     * @param age
     *     The age
     */
    @JsonProperty("age")
    public void setAge(Long age) {
        this.age = age;
    }

    public InmateDetail withAge(Long age) {
        this.age = age;
        return this;
    }

    /**
     * 
     * @return
     *     The physicalAttributes
     */
    @JsonProperty("physicalAttributes")
    public PhysicalAttributes getPhysicalAttributes() {
        return physicalAttributes;
    }

    /**
     * 
     * @param physicalAttributes
     *     The physicalAttributes
     */
    @JsonProperty("physicalAttributes")
    public void setPhysicalAttributes(PhysicalAttributes physicalAttributes) {
        this.physicalAttributes = physicalAttributes;
    }

    public InmateDetail withPhysicalAttributes(PhysicalAttributes physicalAttributes) {
        this.physicalAttributes = physicalAttributes;
        return this;
    }

    /**
     * 
     * @return
     *     The physicalCharacteristics
     */
    @JsonProperty("physicalCharacteristics")
    public List<PhysicalCharacteristic> getPhysicalCharacteristics() {
        return physicalCharacteristics;
    }

    /**
     * 
     * @param physicalCharacteristics
     *     The physicalCharacteristics
     */
    @JsonProperty("physicalCharacteristics")
    public void setPhysicalCharacteristics(List<PhysicalCharacteristic> physicalCharacteristics) {
        this.physicalCharacteristics = physicalCharacteristics;
    }

    public InmateDetail withPhysicalCharacteristics(List<PhysicalCharacteristic> physicalCharacteristics) {
        this.physicalCharacteristics = physicalCharacteristics;
        return this;
    }

    /**
     * 
     * @return
     *     The physicalMarks
     */
    @JsonProperty("physicalMarks")
    public List<PhysicalMark> getPhysicalMarks() {
        return physicalMarks;
    }

    /**
     * 
     * @param physicalMarks
     *     The physicalMarks
     */
    @JsonProperty("physicalMarks")
    public void setPhysicalMarks(List<PhysicalMark> physicalMarks) {
        this.physicalMarks = physicalMarks;
    }

    public InmateDetail withPhysicalMarks(List<PhysicalMark> physicalMarks) {
        this.physicalMarks = physicalMarks;
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

    public InmateDetail withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(inmateId).append(bookingId).append(offenderId).append(firstName).append(middleName).append(lastName).append(alertsCodes).append(agencyId).append(currentLocationId).append(assignedLivingUnitId).append(dateOfBirth).append(age).append(physicalAttributes).append(physicalCharacteristics).append(physicalMarks).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InmateDetail) == false) {
            return false;
        }
        InmateDetail rhs = ((InmateDetail) other);
        return new EqualsBuilder().append(inmateId, rhs.inmateId).append(bookingId, rhs.bookingId).append(offenderId, rhs.offenderId).append(firstName, rhs.firstName).append(middleName, rhs.middleName).append(lastName, rhs.lastName).append(alertsCodes, rhs.alertsCodes).append(agencyId, rhs.agencyId).append(currentLocationId, rhs.currentLocationId).append(assignedLivingUnitId, rhs.assignedLivingUnitId).append(dateOfBirth, rhs.dateOfBirth).append(age, rhs.age).append(physicalAttributes, rhs.physicalAttributes).append(physicalCharacteristics, rhs.physicalCharacteristics).append(physicalMarks, rhs.physicalMarks).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
