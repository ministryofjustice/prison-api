package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Prison details
 **/
@ApiModel(description = "Prison details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrisonDetails {

    @NotBlank
    @ApiModelProperty(value = "ID of prison", example = "MDI")
    private String prisonId;

    @NotBlank
    @ApiModelProperty(value = "Name of prison", example = "Moorland (HMP)")
    private String prison;
}
