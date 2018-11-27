package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Whereabouts Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Whereabouts Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WhereaboutsConfig {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private boolean enabled;

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
      * Whether this prison is enabled for whereabouts
      */
    @ApiModelProperty(required = true, value = "Whether this prison is enabled for whereabouts")
    @JsonProperty("enabled")
    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class WhereaboutsConfig {\n");
        
        sb.append("  enabled: ").append(enabled).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
