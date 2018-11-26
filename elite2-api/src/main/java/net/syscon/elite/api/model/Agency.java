package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

/**
 * Agency Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Agency {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String agencyId;

    @NotBlank
    private String description;

    @NotBlank
    private String agencyType;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Agency identifier.
      */
    @ApiModelProperty(required = true, value = "Agency identifier.")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    /**
      * Agency description.
      */
    @ApiModelProperty(required = true, value = "Agency description.")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
      * Agency type.
      */
    @ApiModelProperty(required = true, value = "Agency type.")
    @JsonProperty("agencyType")
    public String getAgencyType() {
        return agencyType;
    }

    public void setAgencyType(String agencyType) {
        this.agencyType = agencyType;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class Agency {\n");
        
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  agencyType: ").append(agencyType).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
