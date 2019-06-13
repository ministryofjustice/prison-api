package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"establishment", "housing_location"})
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location {

   private CodeDescription establishment;
   @JsonProperty("housing_location")
   private InternalLocation housingLocation;


}
