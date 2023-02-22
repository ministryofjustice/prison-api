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

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * Physical Mark
 **/
@SuppressWarnings("unused")
@Schema(description = "Physical Mark")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PhysicalMark {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    private String type;

    @NotBlank
    private String side;

    @NotBlank
    private String bodyPart;

    @NotBlank
    private String orientation;

    @NotBlank
    private String comment;

    private Long imageId;

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
     * Type of Mark
     */
    @Schema(required = true, description = "Type of Mark")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Left or Right Side
     */
    @Schema(required = true, description = "Left or Right Side")
    @JsonProperty("side")
    public String getSide() {
        return side;
    }

    public void setSide(final String side) {
        this.side = side;
    }

    /**
     * Where on the body
     */
    @Schema(required = true, description = "Where on the body")
    @JsonProperty("bodyPart")
    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(final String bodyPart) {
        this.bodyPart = bodyPart;
    }

    /**
     * Image orientation
     */
    @Schema(required = true, description = "Image orientation")
    @JsonProperty("orientation")
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(final String orientation) {
        this.orientation = orientation;
    }

    /**
     * More information
     */
    @Schema(required = true, description = "More information")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * Image Id Ref
     */
    @Schema(description = "Image Id Ref")
    @JsonProperty("imageId")
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(final Long imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class PhysicalMark {\n");

        sb.append("  type: ").append(type).append("\n");
        sb.append("  side: ").append(side).append("\n");
        sb.append("  bodyPart: ").append(bodyPart).append("\n");
        sb.append("  orientation: ").append(orientation).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("  imageId: ").append(imageId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
