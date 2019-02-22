package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import net.syscon.elite.service.support.LocationProcessor;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "Case Load")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "caseLoadId")
@Data
public class CaseLoad {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @ApiModelProperty(required = true, value = "Case Load ID")
    @JsonProperty("caseLoadId")
    @NotBlank
    private String caseLoadId;

    @ApiModelProperty(required = true, value = "Full description of the case load")
    @JsonProperty("description")
    @NotBlank
    private String description;

    @ApiModelProperty(required = true, value = "Type of case load")
    @JsonProperty("type")
    @NotBlank
    private String type;

    @ApiModelProperty(value = "Functional Use of the case load (nomis only)")
    @JsonProperty("caseloadFunction")
    private String caseloadFunction;

    @ApiModelProperty(required = true, value = "Indicates that this caseload in the context of a staff member is the current active", example = "false")
    @JsonProperty("currentlyActive")
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

    public String getDescription() {
        return LocationProcessor.formatLocation(description);
    }

    @JsonIgnore
    public boolean isAdminType() {
        return "ADMIN".equals(caseloadFunction);
    }
}
