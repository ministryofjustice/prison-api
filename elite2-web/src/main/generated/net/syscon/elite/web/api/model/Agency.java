
package net.syscon.elite.web.api.model;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "agencyId",
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
    private String agencyId;
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agencyType")
    private String agencyType;

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
     * @return
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
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

}
