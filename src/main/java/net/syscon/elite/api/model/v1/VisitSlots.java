package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(description = "Visit slots with capacity")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class VisitSlots {

    @ApiModelProperty(value = "List of visit slots with capacity", allowEmptyValue = true)
    private List<VisitSlotCapacity> slots;
}
