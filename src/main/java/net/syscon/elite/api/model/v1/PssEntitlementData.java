package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"canteen_adjudication", "iep_level"})
public class PssEntitlementData {

    @ApiModelProperty(value = "Canteen adjudication", name = "canteen_adjudication", example = "false", position = 0)
    @JsonProperty("canteen_adjudication")
    private boolean canteenAdj;

    @ApiModelProperty(value = "IEP level code and description", name = "iep_level", position = 1)
    @JsonProperty("iep_level")
    private CodeDescription iepLevel;
}
