package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "IEP Level")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class IepLevel {
    @ApiModelProperty(value = "The IEP level. Must be one of the reference data values in domain 'IEP_LEVEL'. Typically BAS (Basic), ENH (Enhanced), ENT (Entry) or STD (Standard)", required = true, example = "BAS")
    @NotBlank(message = "The IEP level must not be blank")
    private String iepLevel;

    @ApiModelProperty(value = "A long description of the IEP level value", required = true, example = "Basic")
    @NotBlank(message = "The IEP description must not be blank")
    private String iepDescription;

    @ApiModelProperty(value = "Sequence Order of IEP", example = "1")
    private Integer sequence;
}