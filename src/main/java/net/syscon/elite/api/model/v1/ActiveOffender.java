package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(description = "Active Offender")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"found", "offender"})
public class ActiveOffender {

    @ApiModelProperty(value = "found", name = "found", example = "true")
    private boolean found;

    @ApiModelProperty(value = "offender", name = "offender")
    @JsonInclude(Include.NON_NULL)
    private OffenderId offender;
}
