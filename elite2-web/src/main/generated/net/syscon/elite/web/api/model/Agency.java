
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Agency
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "uid",
    "agencyId",
    "description",
    "agencyType"
})
public class Agency {

    @JsonProperty("uid")
    private Long uid;
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
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agencyType")
    private String agencyType;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Agency() {
    }

    /**
     * 
     * @param uid
     * @param description
     * @param agencyId
     * @param agencyType
     */
    public Agency(Long uid, String agencyId, String description, String agencyType) {
        this.uid = uid;
        this.agencyId = agencyId;
        this.description = description;
        this.agencyType = agencyType;
    }

    /**
     * 
     * @return
     *     The uid
     */
    @JsonProperty("uid")
    public Long getUid() {
        return uid;
    }

    /**
     * 
     * @param uid
     *     The uid
     */
    @JsonProperty("uid")
    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Agency withUid(Long uid) {
        this.uid = uid;
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

    public Agency withAgencyId(String agencyId) {
        this.agencyId = agencyId;
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

    public Agency withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The agencyType
     */
    @JsonProperty("agencyType")
    public String getAgencyType() {
        return agencyType;
    }

    /**
     * 
     * (Required)
     * 
     * @param agencyType
     *     The agencyType
     */
    @JsonProperty("agencyType")
    public void setAgencyType(String agencyType) {
        this.agencyType = agencyType;
    }

    public Agency withAgencyType(String agencyType) {
        this.agencyType = agencyType;
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

    public Agency withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uid).append(agencyId).append(description).append(agencyType).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Agency) == false) {
            return false;
        }
        Agency rhs = ((Agency) other);
        return new EqualsBuilder().append(uid, rhs.uid).append(agencyId, rhs.agencyId).append(description, rhs.description).append(agencyType, rhs.agencyType).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
