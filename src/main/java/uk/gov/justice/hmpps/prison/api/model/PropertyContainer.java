package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Offender property container details")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyContainer {
    @Schema(description = "The location id of the property container")
    private Location location;

    @Schema(description = "The case sequence number for the offender", example = "MDI10")
    private String sealMark;

    @Schema(description = "The type of container", example = "Valuables")
    private String containerType;
}
