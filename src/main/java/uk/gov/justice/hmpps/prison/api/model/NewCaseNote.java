package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * New Case Note
 **/
@SuppressWarnings("unused")
@Schema(description = "New Case Note")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewCaseNote {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @Size(max = 12)
    @NotBlank
    private String type;

    @Size(max = 12)
    @NotBlank
    private String subType;

    private LocalDateTime occurrenceDateTime;

    @Size(max = 4000) // For Swagger - custom annotations not well supported
    @MaximumTextSize
    @NotBlank
    private String text;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @Hidden
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Case Note Type
     */
    @Schema(requiredMode = REQUIRED, description = "Case Note Type")
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
    @Schema(requiredMode = REQUIRED, description = "Case Note Sub Type")
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
    @Schema(description = "Date and Time of when case note contact with offender was made")
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
    @Schema(requiredMode = REQUIRED, description = "Case Note Text")
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
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
