package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Agency Establishment Types")
public class AgencyEstablishmentTypes {

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Agency id", example = "MDI")
    private String agencyId;

    @Schema(description = "The establishment types for the agency.")
    @Builder.Default
    private List<AgencyEstablishmentType> establishmentTypes = new ArrayList<>();
}
