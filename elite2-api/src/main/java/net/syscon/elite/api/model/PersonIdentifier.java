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
 * PersonIdentifier
 **/
@SuppressWarnings("unused")
@ApiModel(description = "PersonIdentifier")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PersonIdentifier {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String identifierType;

    @NotBlank
    private String identifierValue;

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
      * The identifier type
      */
    @ApiModelProperty(required = true, value = "The identifier type")
    @JsonProperty("identifierType")
    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    /**
      * The most recent identifier value of that type.
      */
    @ApiModelProperty(required = true, value = "The most recent identifier value of that type.")
    @JsonProperty("identifierValue")
    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class PersonIdentifier {\n");
        
        sb.append("  identifierType: ").append(identifierType).append("\n");
        sb.append("  identifierValue: ").append(identifierValue).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
