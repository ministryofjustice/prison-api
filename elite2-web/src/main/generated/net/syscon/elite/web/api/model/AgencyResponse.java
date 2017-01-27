
package net.syscon.elite.web.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agencyId",
    "description",
    "agencyType"
})
public class AgencyResponse {

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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agencyId).append(description).append(agencyType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgencyResponse) == false) {
            return false;
        }
        AgencyResponse rhs = ((AgencyResponse) other);
        return new EqualsBuilder().append(agencyId, rhs.agencyId).append(description, rhs.description).append(agencyType, rhs.agencyType).isEquals();
    }

}
