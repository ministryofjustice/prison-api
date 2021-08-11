package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Image Detail
 **/

@ApiModel(description = "Image Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class ImageDetail {

    @ApiModelProperty(required = true, value = "Image ID")
    @NotNull
    private Long imageId;

    @ApiModelProperty(required = true, value = "Date of image capture")
    @NotNull
    private LocalDate captureDate;

    @ApiModelProperty(required = true, value = "Image view information")
    @NotBlank
    private String imageView;

    @ApiModelProperty(required = true, value = "Orientation of the image")
    @NotBlank
    private String imageOrientation;

    @ApiModelProperty(required = true, value = "Image Type")
    @NotBlank
    private String imageType;

    @ApiModelProperty(value = "Object ID")
    private Long objectId;

}
