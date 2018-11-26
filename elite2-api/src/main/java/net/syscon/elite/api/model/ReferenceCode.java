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
import java.util.List;
import java.util.Map;

/**
 * Reference Code
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Reference Code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ReferenceCode {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String domain;

    @NotBlank
    private String code;

    @NotBlank
    private String description;

    private String parentDomain;

    private String parentCode;

    @NotBlank
    private String activeFlag;

    private List<ReferenceCode> subCodes;

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
      * Reference data item domain.
      */
    @ApiModelProperty(required = true, value = "Reference data item domain.")
    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
      * Reference data item code.
      */
    @ApiModelProperty(required = true, value = "Reference data item code.")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
      * Reference data item description.
      */
    @ApiModelProperty(required = true, value = "Reference data item description.")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
      * Parent reference data item domain.
      */
    @ApiModelProperty(value = "Parent reference data item domain.")
    @JsonProperty("parentDomain")
    public String getParentDomain() {
        return parentDomain;
    }

    public void setParentDomain(String parentDomain) {
        this.parentDomain = parentDomain;
    }

    /**
      * Parent reference data item code.
      */
    @ApiModelProperty(value = "Parent reference data item code.")
    @JsonProperty("parentCode")
    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    /**
      * Reference data item active indicator flag.
      */
    @ApiModelProperty(required = true, value = "Reference data item active indicator flag.")
    @JsonProperty("activeFlag")
    public String getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(String activeFlag) {
        this.activeFlag = activeFlag;
    }

    /**
      * List of subordinate reference data items associated with this reference data item.
      */
    @ApiModelProperty(value = "List of subordinate reference data items associated with this reference data item.")
    @JsonProperty("subCodes")
    public List<ReferenceCode> getSubCodes() {
        return subCodes;
    }

    public void setSubCodes(List<ReferenceCode> subCodes) {
        this.subCodes = subCodes;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class ReferenceCode {\n");
        
        sb.append("  domain: ").append(domain).append("\n");
        sb.append("  code: ").append(code).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  parentDomain: ").append(parentDomain).append("\n");
        sb.append("  parentCode: ").append(parentCode).append("\n");
        sb.append("  activeFlag: ").append(activeFlag).append("\n");
        sb.append("  subCodes: ").append(subCodes).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
