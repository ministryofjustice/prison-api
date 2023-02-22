package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(required = true, description = "Image ID", example = "2461788")
    @NotNull
    private Long imageId;

    @Schema(required = true, description = "Date of image capture", example = "2008-08-27")
    @NotNull
    private LocalDate captureDate;

    @Schema(required = true, description = "Image view information", example = "FACE")
    @NotBlank
    private String imageView;

    @Schema(required = true, description = "Orientation of the image", example = "FRONT")
    @NotBlank
    private String imageOrientation;

    @Schema(required = true, description = "Image Type", example = "OFF_BKG")
    @NotBlank
    private String imageType;

    @Schema(description = "Object ID")
    private Long objectId;

}
