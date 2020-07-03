package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Offender property container details")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyContainer {
    @ApiModelProperty(value = "The location id of the property container")
    private Location location;

    @ApiModelProperty(value = "The case sequence number for the offender", example = "MDI10")
    private String sealMark;

    @ApiModelProperty(value = "The type of container", example = "Valuables")
    private String containerType;
}
