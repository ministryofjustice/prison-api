package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Location")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"establishment", "housing_location"})
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location {

    @Schema(description = "Establishment", example = "{code: 'BMI', desc: 'BIRMINGHAM (HMP)'}")
    private CodeDescription establishment;

    @Schema(description = "Housing Location", name = "housing_location", example = "[{ type: 'Wing', value: 'C' },{ type: 'Landing', value: '2' },{ type: 'Cell', value: '03' }]")
    @JsonProperty("housing_location")
    private InternalLocation housingLocation;


}
