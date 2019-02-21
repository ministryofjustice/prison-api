package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.syscon.elite.service.support.LocationProcessor;

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

    @NotBlank
    private boolean currentlyActive;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
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

    public void setCaseLoadId(final String caseLoadId) {
        this.caseLoadId = caseLoadId;
    }

    /**
     * Full description of the case load
     */
    @ApiModelProperty(required = true, value = "Full description of the case load")
    @JsonProperty("description")
    public String getDescription() {
        return LocationProcessor.formatLocation(description);
    }

    public void setDescription(final String description) {
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

    public void setType(final String type) {
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

    public void setCaseloadFunction(final String caseloadFunction) {
        this.caseloadFunction = caseloadFunction;
    }

    @ApiModelProperty(required = true, value = "Indicates that this caseload in the context of a staff member is the current active", example = "false")
    @JsonProperty("currentlyActive")
    public boolean isCurrentlyActive() {
        return currentlyActive;
    }

    public void setCurrentlyActive(final boolean currentlyActive) {
        this.currentlyActive = currentlyActive;
    }

    @Override
    public String toString() {
        return String.format("class CaseLoad {\n" +
                "  caseLoadId: %s\n" +
                "  description: %s\n" +
                "  type: %s\n" +
                "  caseloadFunction: %s\n" +
                "}\n", caseLoadId, description, type, caseloadFunction);
    }
}
