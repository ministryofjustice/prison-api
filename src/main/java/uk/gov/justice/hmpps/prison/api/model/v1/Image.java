package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Prisoner Photo")
@Data
@Builder
@AllArgsConstructor
public class Image {
    @Schema(description = "Base64 Encoded JPEG data", example = "<base64_encoded_jpeg_data>")
    private String image;
}
