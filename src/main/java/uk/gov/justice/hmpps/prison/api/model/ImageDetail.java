package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Image ID", example = "2461788")
    @NotNull
    private Long imageId;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "An active image means that it is to be used and is current for the prisoner. An inactive image means that it has been superseded by another image or the image is no longer relevant.", example = "false")
    private boolean active;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Date of image capture", example = "2008-08-27")
    @NotNull
    private LocalDate captureDate;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Date and time of image capture", example = "2008-08-28T01:01:01")
    @NotNull
    private LocalDateTime captureDateTime;

    @Schema(requiredMode = RequiredMode.REQUIRED,
        description = "Image view information. Actual values extracted 10/05/2023, with the majority of values being FACE. This doesn't appear to be mapped to any REFERENCE_CODE data, even though there is a domain called IMAGE_VIEW.",
        example = "FACE",
        allowableValues = {"OIC", "FACE", "TAT", "MARK", "SCAR", "OTH"})
    @NotBlank
    private String imageView;

    @Schema(requiredMode = RequiredMode.REQUIRED,
        description = "Orientation of the image. Actual values extracted 10/05/2023, with the majority of values being FRONT. This doesn't appear to be mapped to any REFERENCE_CODE data, even though there is a domain called PART_ORIENT.",
        example = "FRONT",
        allowableValues = {"NECK", "KNEE", "TORSO", "FACE", "DAMAGE", "INJURY", "HAND", "HEAD", "THIGH", "ELBOW", "FOOT", "INCIDENT", "ARM", "SHOULDER", "ANKLE", "FINGER", "EAR", "TOE", "FIGHT", "FRONT", "LEG", "LIP", "NOSE"}
    )
    @NotBlank
    private String imageOrientation;

    @Schema(requiredMode = RequiredMode.REQUIRED,
        description = "Image Type. Actual values extracted 10/05/2023, with the majority of values being OFF_BKG. This doesn't appear to be mapped to any REFERENCE_CODE data.",
        example = "OFF_BKG",
        allowableValues = {"OFF_IDM", "OFF_BKG", "OIC"}
    )
    @NotBlank
    private String imageType;

    @Schema(description = "Object ID")
    private Long objectId;

}
