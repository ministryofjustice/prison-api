package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Agency {
    @NotBlank
    @ApiModelProperty(required = true, value = "Agency identifier.", example = "MDI")
    private String agencyId;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency description.", example = "Moorland (HMP & YOI)")
    private String description;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency type.", example = "INST")
    private String agencyType;

    @ApiModelProperty(value = "Agency is active", example = "true")
    private boolean active;
}
