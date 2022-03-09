package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "Agency Establishment Types")
public class AgencyEstablishmentTypes {

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency id", example = "MDI", position = 1)
    private String agencyId;

    @ApiModelProperty(value = "The establishment types for the agency.", position = 2)
    @Builder.Default
    private List<AgencyEstablishmentType> establishmentTypes = new ArrayList<>();
}
