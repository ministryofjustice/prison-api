package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Agency Establishment Type")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AgencyEstablishmentType {

    @NotBlank
    @ApiModelProperty(required = true, value = "Code.", example = "IM", position = 1)
    private String code;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description.", example = "Closed Young Offender Institute (Male)", position = 2)
    private String description;
}
