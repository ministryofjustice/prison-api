package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(description = "List of visitors for a visit")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VisitWithVisitors<V extends Visit> {
    @ApiModelProperty(value = "List of visitors on visit")
    @JsonProperty("visitors")
    private List<Visitor> visitors;

    @ApiModelProperty(value = "Visit Information")
    @JsonProperty("visitDetails")
    private V visitDetail;
}
