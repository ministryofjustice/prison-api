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
@JsonPropertyOrder({"agency_location", "internal_location", "location_type"})
public class PssLocationData {

    @ApiModelProperty(value = "Agency location code", name = "agency_location", example = "LEI", position = 0)
    @JsonProperty("agency_location")
    private String agyLoc;

    @ApiModelProperty(value = "Internal location code", name = "internal_location", example = "LEI-E-5-004", position = 1)
    @JsonProperty("internal_location")
    private String internalLocation;

    @ApiModelProperty(value = "Location type", name = "location_type", example = "CELL", position = 2)
    @JsonProperty("location_type")
    private String locationType;
}
