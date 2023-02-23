package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Agency Establishment Type")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AgencyEstablishmentType {

    @NotBlank
    @Schema(required = true, description = "Code.", example = "IM")
    private String code;

    @NotBlank
    @Schema(required = true, description = "Description.", example = "Closed Young Offender Institute (Male)")
    private String description;
}
