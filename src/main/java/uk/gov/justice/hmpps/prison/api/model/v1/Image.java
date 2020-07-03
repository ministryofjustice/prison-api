package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@ApiModel(description = "Prisoner Photo")
@Data
@Builder
@AllArgsConstructor
public class Image {
    @ApiModelProperty(value = "Base64 Encoded JPEG data", example = "<base64_encoded_jpeg_data>")
    private String image;
}
