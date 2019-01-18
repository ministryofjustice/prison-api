package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * Case Load
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Load")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "caseLoadId")
public class CaseLoad {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String caseLoadId;

    @NotBlank
    private String description;

    @NotBlank
    private String type;

    private String caseloadFunction;

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
      * Case Load ID
      */
    @ApiModelProperty(required = true, value = "Case Load ID")
    @JsonProperty("caseLoadId")
    public String getCaseLoadId() {
        return caseLoadId;
    }

    public void setCaseLoadId(String caseLoadId) {
        this.caseLoadId = caseLoadId;
    }

    /**
      * Full description of the case load
      */
    @ApiModelProperty(required = true, value = "Full description of the case load")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
      * Type of case load
      */
    @ApiModelProperty(required = true, value = "Type of case load")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public boolean isAdminType() {
        return "ADMIN".equals(caseloadFunction);
    }
    /**
      * Functional Use of the case load (nomis only)
      */
    @ApiModelProperty(value = "Functional Use of the case load (nomis only)")
    @JsonProperty("caseloadFunction")
    public String getCaseloadFunction() {
        return caseloadFunction;
    }

    public void setCaseloadFunction(String caseloadFunction) {
        this.caseloadFunction = caseloadFunction;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CaseLoad {\n");
        
        sb.append("  caseLoadId: ").append(caseLoadId).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  caseloadFunction: ").append(caseloadFunction).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
