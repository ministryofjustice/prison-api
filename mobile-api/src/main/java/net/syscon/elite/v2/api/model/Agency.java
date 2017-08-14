package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "agencyId",
        "description",
        "agencyType"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Agency {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @JsonProperty("agencyId")
    private String agencyId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("agencyType")
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

    @JsonProperty("agencyId")
    public String getAgencyId() {
        return this.agencyId;
    }

    @ApiModelProperty(value = "Unique identifier for agency.", required = true, position = 1)
    @JsonProperty("agencyId")
    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    @ApiModelProperty(value = "Agency description.", required = true, position = 2)
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("agencyType")
    public String getAgencyType() {
        return this.agencyType;
    }

    @ApiModelProperty(value = "Agency type.", required = true, position = 3)
    @JsonProperty("agencyType")
    public void setAgencyType(String agencyType) {
        this.agencyType = agencyType;
    }
}
