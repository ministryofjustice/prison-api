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
 * Physical Mark
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Physical Mark")
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

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Type of Mark
      */
    @ApiModelProperty(required = true, value = "Type of Mark")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
      * Left or Right Side
      */
    @ApiModelProperty(required = true, value = "Left or Right Side")
    @JsonProperty("side")
    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    /**
      * Where on the body
      */
    @ApiModelProperty(required = true, value = "Where on the body")
    @JsonProperty("bodyPart")
    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    /**
      * Image orientation
      */
    @ApiModelProperty(required = true, value = "Image orientation")
    @JsonProperty("orientation")
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    /**
      * More information
      */
    @ApiModelProperty(required = true, value = "More information")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
      * Image Id Ref
      */
    @ApiModelProperty(value = "Image Id Ref")
    @JsonProperty("imageId")
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

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
