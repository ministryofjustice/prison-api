package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * Telephone Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Telephone Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Telephone {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    private String number;

    @NotBlank
    private String type;

    private String ext;

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
     * Telephone number
     */
    @ApiModelProperty(required = true, value = "Telephone number")
    @JsonProperty("number")
    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    /**
     * Telephone type
     */
    @ApiModelProperty(required = true, value = "Telephone type")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Telephone extention number
     */
    @ApiModelProperty(value = "Telephone extention number")
    @JsonProperty("ext")
    public String getExt() {
        return ext;
    }

    public void setExt(final String ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class Telephone {\n");

        sb.append("  number: ").append(number).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  ext: ").append(ext).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
