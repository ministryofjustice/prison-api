
package net.syscon.elite.web.api.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Movement
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "moveCategory",
    "inmateId",
    "moveDateTime",
    "fromAgencyId",
    "toAgencyId",
    "moveType",
    "moveReason",
    "fromLocationId",
    "toLocationId"
})
public class Movement {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("moveCategory")
    private Movement.MoveCategory moveCategory;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("inmateId")
    private Long inmateId;
    @JsonProperty("moveDateTime")
    private String moveDateTime;
    @JsonProperty("fromAgencyId")
    private String fromAgencyId;
    @JsonProperty("toAgencyId")
    private String toAgencyId;
    @JsonProperty("moveType")
    private String moveType;
    @JsonProperty("moveReason")
    private String moveReason;
    @JsonProperty("fromLocationId")
    private String fromLocationId;
    @JsonProperty("toLocationId")
    private String toLocationId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Movement() {
    }

    /**
     * 
     * @param moveDateTime
     * @param inmateId
     * @param fromAgencyId
     * @param toLocationId
     * @param moveCategory
     * @param toAgencyId
     * @param fromLocationId
     * @param moveType
     * @param moveReason
     */
    public Movement(Movement.MoveCategory moveCategory, Long inmateId, String moveDateTime, String fromAgencyId, String toAgencyId, String moveType, String moveReason, String fromLocationId, String toLocationId) {
        this.moveCategory = moveCategory;
        this.inmateId = inmateId;
        this.moveDateTime = moveDateTime;
        this.fromAgencyId = fromAgencyId;
        this.toAgencyId = toAgencyId;
        this.moveType = moveType;
        this.moveReason = moveReason;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The moveCategory
     */
    @JsonProperty("moveCategory")
    public Movement.MoveCategory getMoveCategory() {
        return moveCategory;
    }

    /**
     * 
     * (Required)
     * 
     * @param moveCategory
     *     The moveCategory
     */
    @JsonProperty("moveCategory")
    public void setMoveCategory(Movement.MoveCategory moveCategory) {
        this.moveCategory = moveCategory;
    }

    public Movement withMoveCategory(Movement.MoveCategory moveCategory) {
        this.moveCategory = moveCategory;
        return this;
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

    public Movement withInmateId(Long inmateId) {
        this.inmateId = inmateId;
        return this;
    }

    /**
     * 
     * @return
     *     The moveDateTime
     */
    @JsonProperty("moveDateTime")
    public String getMoveDateTime() {
        return moveDateTime;
    }

    /**
     * 
     * @param moveDateTime
     *     The moveDateTime
     */
    @JsonProperty("moveDateTime")
    public void setMoveDateTime(String moveDateTime) {
        this.moveDateTime = moveDateTime;
    }

    public Movement withMoveDateTime(String moveDateTime) {
        this.moveDateTime = moveDateTime;
        return this;
    }

    /**
     * 
     * @return
     *     The fromAgencyId
     */
    @JsonProperty("fromAgencyId")
    public String getFromAgencyId() {
        return fromAgencyId;
    }

    /**
     * 
     * @param fromAgencyId
     *     The fromAgencyId
     */
    @JsonProperty("fromAgencyId")
    public void setFromAgencyId(String fromAgencyId) {
        this.fromAgencyId = fromAgencyId;
    }

    public Movement withFromAgencyId(String fromAgencyId) {
        this.fromAgencyId = fromAgencyId;
        return this;
    }

    /**
     * 
     * @return
     *     The toAgencyId
     */
    @JsonProperty("toAgencyId")
    public String getToAgencyId() {
        return toAgencyId;
    }

    /**
     * 
     * @param toAgencyId
     *     The toAgencyId
     */
    @JsonProperty("toAgencyId")
    public void setToAgencyId(String toAgencyId) {
        this.toAgencyId = toAgencyId;
    }

    public Movement withToAgencyId(String toAgencyId) {
        this.toAgencyId = toAgencyId;
        return this;
    }

    /**
     * 
     * @return
     *     The moveType
     */
    @JsonProperty("moveType")
    public String getMoveType() {
        return moveType;
    }

    /**
     * 
     * @param moveType
     *     The moveType
     */
    @JsonProperty("moveType")
    public void setMoveType(String moveType) {
        this.moveType = moveType;
    }

    public Movement withMoveType(String moveType) {
        this.moveType = moveType;
        return this;
    }

    /**
     * 
     * @return
     *     The moveReason
     */
    @JsonProperty("moveReason")
    public String getMoveReason() {
        return moveReason;
    }

    /**
     * 
     * @param moveReason
     *     The moveReason
     */
    @JsonProperty("moveReason")
    public void setMoveReason(String moveReason) {
        this.moveReason = moveReason;
    }

    public Movement withMoveReason(String moveReason) {
        this.moveReason = moveReason;
        return this;
    }

    /**
     * 
     * @return
     *     The fromLocationId
     */
    @JsonProperty("fromLocationId")
    public String getFromLocationId() {
        return fromLocationId;
    }

    /**
     * 
     * @param fromLocationId
     *     The fromLocationId
     */
    @JsonProperty("fromLocationId")
    public void setFromLocationId(String fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public Movement withFromLocationId(String fromLocationId) {
        this.fromLocationId = fromLocationId;
        return this;
    }

    /**
     * 
     * @return
     *     The toLocationId
     */
    @JsonProperty("toLocationId")
    public String getToLocationId() {
        return toLocationId;
    }

    /**
     * 
     * @param toLocationId
     *     The toLocationId
     */
    @JsonProperty("toLocationId")
    public void setToLocationId(String toLocationId) {
        this.toLocationId = toLocationId;
    }

    public Movement withToLocationId(String toLocationId) {
        this.toLocationId = toLocationId;
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

    public Movement withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(moveCategory).append(inmateId).append(moveDateTime).append(fromAgencyId).append(toAgencyId).append(moveType).append(moveReason).append(fromLocationId).append(toLocationId).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Movement) == false) {
            return false;
        }
        Movement rhs = ((Movement) other);
        return new EqualsBuilder().append(moveCategory, rhs.moveCategory).append(inmateId, rhs.inmateId).append(moveDateTime, rhs.moveDateTime).append(fromAgencyId, rhs.fromAgencyId).append(toAgencyId, rhs.toAgencyId).append(moveType, rhs.moveType).append(moveReason, rhs.moveReason).append(fromLocationId, rhs.fromLocationId).append(toLocationId, rhs.toLocationId).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum MoveCategory {

        EXTERNAL("external"),
        INTERNAL("internal");
        private final String value;
        private final static Map<String, Movement.MoveCategory> CONSTANTS = new HashMap<String, Movement.MoveCategory>();

        static {
            for (Movement.MoveCategory c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private MoveCategory(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Movement.MoveCategory fromValue(String value) {
            Movement.MoveCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
