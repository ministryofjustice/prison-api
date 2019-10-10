package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Offence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenceDetail {

    @ApiModelProperty(required = true, value = "Prisoner booking id", example = "1123456", position = 1)
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Description of offence", position = 2)
    @NotBlank
    private String offenceDescription;
}
