package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(description = "Type Value")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TypeValue {

   @ApiModelProperty(value = "Type", position = 0, example = "Wing")
   private String type;

   @ApiModelProperty(value = "Value", position = 1, example = "C")
   private String value;
}
