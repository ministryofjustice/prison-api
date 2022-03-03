package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Location")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"establishment", "housing_location"})
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location {

    @ApiModelProperty(value = "Establishment", position = 0, example = "{code: 'BMI', desc: 'BIRMINGHAM (HMP)'}")
    private CodeDescription establishment;

    @ApiModelProperty(value = "Housing Location", name = "housing_location", position = 1, example = "[{ type: 'Wing', value: 'C' },{ type: 'Landing', value: '2' },{ type: 'Cell', value: '03' }]")
    @JsonProperty("housing_location")
    private InternalLocation housingLocation;


}
