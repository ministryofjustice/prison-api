package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
    @NotNull
    private boolean enabled;

    /**
      * Whether this prison is enabled for whereabouts
      */
    @ApiModelProperty(required = true, value = "Whether this prison is enabled for whereabouts")
    @JsonProperty("enabled")
    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class WhereaboutsConfig {\n");
        
        sb.append("  enabled: ").append(enabled).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
