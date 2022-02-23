package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Transfer Response")
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Transfer {
    @Schema(description = "Current Location", name = "current_location")
    @JsonProperty("current_location")
    public CodeDescription currentLocation;
    @Schema(description = "Transaction", name = "transaction")
    public Transaction transaction;
}
