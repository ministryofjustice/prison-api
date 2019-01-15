package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cell Locations are grouped for unlock lists as a 2 level tree. The two levels are referred to as Location and Sub-Location in the prisonstaffhub UI. Each (location/sub-location) group has a name that is understood by prison officers and also serves as a key to retrieve the corresponding Cell Locations and information about their occupants.
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Cell Locations are grouped for unlock lists as a 2 level tree. The two levels are referred to as Location and Sub-Location in the prisonstaffhub UI. Each (location/sub-location) group has a name that is understood by prison officers and also serves as a key to retrieve the corresponding Cell Locations and information about their occupants.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LocationGroup {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String name;

    @NotNull
    @Builder.Default
    private List<LocationGroup> children = new ArrayList<LocationGroup>();

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
      * The name of the group
      */
    @ApiModelProperty(required = true, value = "The name of the group")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
      * The child groups of this group
      */
    @ApiModelProperty(required = true, value = "The child groups of this group")
    @JsonProperty("children")
    public List<LocationGroup> getChildren() {
        return children;
    }

    public void setChildren(List<LocationGroup> children) {
        this.children = children;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class LocationGroup {\n");
        
        sb.append("  name: ").append(name).append("\n");
        sb.append("  children: ").append(children).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
