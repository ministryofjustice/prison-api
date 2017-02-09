
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
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Agency
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "agencyId",
    "agencyCode",
    "description",
    "agencyType"
})
public class Agency {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agencyId")
    private Long agencyId;
    @JsonProperty("agencyCode")
    private String agencyCode;
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
     * 
     * (Required)
     * 
     * @return
     *     The agencyId
     */
    @JsonProperty("agencyId")
    public Long getAgencyId() {
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
    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public Agency withAgencyId(Long agencyId) {
        this.agencyId = agencyId;
        return this;
    }

    /**
     * 
     * @return
     *     The agencyCode
     */
    @JsonProperty("agencyCode")
    public String getAgencyCode() {
        return agencyCode;
    }

    /**
     * 
     * @param agencyCode
     *     The agencyCode
     */
    @JsonProperty("agencyCode")
    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    public Agency withAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
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

}
