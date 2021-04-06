package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

public class RequestToUpdatePhone {

    @ApiModelProperty(required = true, value = "Telephone number")
    @NotBlank
    private String number;

    @ApiModelProperty(required = true, value = "Telephone type")
    @NotBlank
    private String type;

    @ApiModelProperty(value = "Telephone extension number")
    private String ext;
}
