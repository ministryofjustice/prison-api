package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Alert subtype")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AlertSubtype {
    @ApiModelProperty(value = "The code for the alert subtype", required = true, example = "AS")
    @NotBlank(message = "Alert subtype code cannot be blank")
    private String code;

    @ApiModelProperty(value = "The description for the alert subtype", required = true, example = "Social Care")
    @NotBlank(message = "Alert subtype description cannot be blank")
    private String description;

    @ApiModelProperty(value = "The position of the alert subtype in the list of subtypes", required = true, example = "1")
    @NotBlank(message = "Alert subtype list sequence cannot be blank")
    private Integer listSeq;

    @ApiModelProperty(value = "The alert code of the parent Alert type", required = true, example = "A")
    @NotBlank(message = "Alert subtype parent code cannot be blank")
    private String parentCode;
}
