package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Update Phone Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestToUpdatePhone {

    @Schema(required = true, description = "Telephone number")
    @NotBlank
    private String number;

    @Schema(required = true, description = "Telephone type")
    @NotBlank
    private String type;

    @Schema(description = "Telephone extension number")
    private String ext;
}
