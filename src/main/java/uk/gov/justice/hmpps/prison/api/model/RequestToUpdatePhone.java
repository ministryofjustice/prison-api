package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Update Phone Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
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
