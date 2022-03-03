package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel(description = "Transfer Response")
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Transfer {
    @ApiModelProperty(value = "Current Location", name = "current_location", position = 1)
    @JsonProperty("current_location")
    public CodeDescription currentLocation;
    @ApiModelProperty(value = "Transaction", name = "transaction", position = 2)
    public Transaction transaction;
}
