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
import java.time.LocalDate;

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

    /**
     * Image ID
     */
    @ApiModelProperty(required = true, value = "Image ID")
    @JsonProperty("imageId")
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(final Long imageId) {
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

    public void setCaptureDate(final LocalDate captureDate) {
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

    public void setImageView(final String imageView) {
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

    public void setImageOrientation(final String imageOrientation) {
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

    public void setImageType(final String imageType) {
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

    public void setObjectId(final Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class ImageDetail {\n");

        sb.append("  imageId: ").append(imageId).append("\n");
        sb.append("  captureDate: ").append(captureDate).append("\n");
        sb.append("  imageView: ").append(imageView).append("\n");
        sb.append("  imageOrientation: ").append(imageOrientation).append("\n");
        sb.append("  imageType: ").append(imageType).append("\n");
        sb.append("  objectId: ").append(objectId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
