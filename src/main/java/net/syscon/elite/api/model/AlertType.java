package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Alert type")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AlertType {
    @ApiModelProperty(value = "The code for the alert type", required = true, example = "A")
    @NotBlank(message = "Alert type code cannot be blank")
    private String code;

    @ApiModelProperty(value = "The description for the alert type", required = true, example = "Social Care")
    @NotBlank(message = "Alert type description cannot be blank")
    private String description;

    @ApiModelProperty(value = "The position of the alert type in the list of types", required = true, example = "12")
    @NotBlank(message = "Alert type list sequence cannot be blank")
    private Integer listSeq;

}
