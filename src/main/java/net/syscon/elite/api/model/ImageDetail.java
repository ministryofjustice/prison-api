package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Image Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Image Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ImageDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long imageId;

    @NotNull
    private LocalDate captureDate;

    @NotBlank
    private String imageView;

    @NotBlank
    private String imageOrientation;

    @NotBlank
    private String imageType;

    private Long objectId;

    private String imageData;

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
      * Image ID
      */
    @ApiModelProperty(required = true, value = "Image ID")
    @JsonProperty("imageId")
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    /**
      * Date of image capture
      */
    @ApiModelProperty(required = true, value = "Date of image capture")
    @JsonProperty("captureDate")
    public LocalDate getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(LocalDate captureDate) {
        this.captureDate = captureDate;
    }

    /**
      * Image view information
      */
    @ApiModelProperty(required = true, value = "Image view information")
    @JsonProperty("imageView")
    public String getImageView() {
        return imageView;
    }

    public void setImageView(String imageView) {
        this.imageView = imageView;
    }

    /**
      * Orientation of the image
      */
    @ApiModelProperty(required = true, value = "Orientation of the image")
    @JsonProperty("imageOrientation")
    public String getImageOrientation() {
        return imageOrientation;
    }

    public void setImageOrientation(String imageOrientation) {
        this.imageOrientation = imageOrientation;
    }

    /**
      * Image Type
      */
    @ApiModelProperty(required = true, value = "Image Type")
    @JsonProperty("imageType")
    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
      * Object ID
      */
    @ApiModelProperty(value = "Object ID")
    @JsonProperty("objectId")
    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    /**
      * Bytes of Image
      */
    @ApiModelProperty(value = "Bytes of Image")
    @JsonProperty("imageData")
    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class ImageDetail {\n");
        
        sb.append("  imageId: ").append(imageId).append("\n");
        sb.append("  captureDate: ").append(captureDate).append("\n");
        sb.append("  imageView: ").append(imageView).append("\n");
        sb.append("  imageOrientation: ").append(imageOrientation).append("\n");
        sb.append("  imageType: ").append(imageType).append("\n");
        sb.append("  objectId: ").append(objectId).append("\n");
        sb.append("  imageData: ").append(imageData).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
