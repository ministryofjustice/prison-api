package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Schema(description = "Image Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class ImageDetail {

    @Schema(required = true, description = "Image ID")
    @NotNull
    private Long imageId;

    @Schema(required = true, description = "Date of image capture")
    @NotNull
    private LocalDate captureDate;

    @Schema(required = true, description = "Image view information")
    @NotBlank
    private String imageView;

    @Schema(required = true, description = "Orientation of the image")
    @NotBlank
    private String imageOrientation;

    @Schema(required = true, description = "Image Type")
    @NotBlank
    private String imageType;

    @Schema(description = "Object ID")
    private Long objectId;

}
