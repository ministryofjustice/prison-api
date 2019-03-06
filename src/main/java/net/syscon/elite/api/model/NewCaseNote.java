package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * New Case Note
 **/
@SuppressWarnings("unused")
@ApiModel(description = "New Case Note")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewCaseNote {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @Length(max=12) @NotBlank
    private String type;

    @Length(max=12) @NotBlank
    private String subType;

    private LocalDateTime occurrenceDateTime;

    @Length(max=4000) @NotBlank
    private String text;

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
      * Case Note Type
      */
    @ApiModelProperty(required = true, value = "Case Note Type")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
      * Case Note Sub Type
      */
    @ApiModelProperty(required = true, value = "Case Note Sub Type")
    @JsonProperty("subType")
    public String getSubType() {
        return subType;
    }

    public void setSubType(final String subType) {
        this.subType = subType;
    }

    /**
      * Date and Time of when case note contact with offender was made
      */
    @ApiModelProperty(value = "Date and Time of when case note contact with offender was made")
    @JsonProperty("occurrenceDateTime")
    public LocalDateTime getOccurrenceDateTime() {
        return occurrenceDateTime;
    }

    public void setOccurrenceDateTime(final LocalDateTime occurrenceDateTime) {
        this.occurrenceDateTime = occurrenceDateTime;
    }

    /**
      * Case Note Text
      */
    @ApiModelProperty(required = true, value = "Case Note Text")
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class NewCaseNote {\n");
        
        sb.append("  type: ").append(type).append("\n");
        sb.append("  subType: ").append(subType).append("\n");
        sb.append("  occurrenceDateTime: ").append(occurrenceDateTime).append("\n");
        sb.append("  text: ").append(text).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
