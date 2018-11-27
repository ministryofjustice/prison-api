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
 * Establishment roll count in and out numbers
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Establishment roll count in and out numbers")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MovementCount {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Integer in;

    @NotNull
    private Integer out;

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
      * Number of prisoners arrived so far on given date
      */
    @ApiModelProperty(required = true, value = "Number of prisoners arrived so far on given date")
    @JsonProperty("in")
    public Integer getIn() {
        return in;
    }

    public void setIn(Integer in) {
        this.in = in;
    }

    /**
      * Number of prisoners that have left so far on given date
      */
    @ApiModelProperty(required = true, value = "Number of prisoners that have left so far on given date")
    @JsonProperty("out")
    public Integer getOut() {
        return out;
    }

    public void setOut(Integer out) {
        this.out = out;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class MovementCount {\n");
        
        sb.append("  in: ").append(in).append("\n");
        sb.append("  out: ").append(out).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
