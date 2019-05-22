package net.syscon.elite.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "A type of offence that can be made as part of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationOffence {

    @ApiModelProperty("Offence Id")
    private String id;
    @ApiModelProperty("Offence Code")
    private String code;
    @ApiModelProperty("Offence Description")
    private String description;
}