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
 * Profile Information
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Profile Information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ProfileInformation {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String type;

    @NotBlank
    private String question;

    @NotBlank
    private String resultValue;

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
      * Type of profile information
      */
    @ApiModelProperty(required = true, value = "Type of profile information")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
      * Profile Question
      */
    @ApiModelProperty(required = true, value = "Profile Question")
    @JsonProperty("question")
    public String getQuestion() {
        return question;
    }

    public void setQuestion(final String question) {
        this.question = question;
    }

    /**
      * Profile Result Answer
      */
    @ApiModelProperty(required = true, value = "Profile Result Answer")
    @JsonProperty("resultValue")
    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(final String resultValue) {
        this.resultValue = resultValue;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class ProfileInformation {\n");
        
        sb.append("  type: ").append(type).append("\n");
        sb.append("  question: ").append(question).append("\n");
        sb.append("  resultValue: ").append(resultValue).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
