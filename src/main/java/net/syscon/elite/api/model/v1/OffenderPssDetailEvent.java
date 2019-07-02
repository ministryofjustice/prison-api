package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

@ApiModel(description = "OffenderPssDetailEvent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "eventType", "nomsId", "eventData"})
@JsonAutoDetect(
        fieldVisibility = Visibility.ANY,
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"type", "id", "timestamp", "prison_id", "noms_id", "offender_details_request"})
public class OffenderPssDetailEvent {

    @ApiModelProperty(value = "Event type", name = "type", example = "offender_details_request", position = 0)
    @JsonProperty("type")
    private String eventType;

    @ApiModelProperty(value = "Event identifier (always 0)", name = "id", example = "0", position = 1)
    private Long id;

    @ApiModelProperty(value = "Event date and time", name = "timestamp", example = "2019-04-23 14:23.000", position = 2)
    @JsonProperty("timestamp")
    private LocalDateTime eventTimeStamp;

    @ApiModelProperty(value = "Prison identifier", name = "prison_id", example = "MDI", position = 3)
    @JsonProperty("prison_id")
    private String prisonId;

    @ApiModelProperty(value = "Noms identifier", name = "noms_id", example = "A1404AE", position = 4)
    @JsonProperty("noms_id")
    private String nomsId;

    // The event data is a string containing a complex JSON object generated directly from the PL/SQL procedure api_offender_procs.pss_offender_details.
    // We do not have this object modelled in Java so not part of the swagger documentation though the endpoint notes contain the response format
    @ApiModelProperty(value = "Offender details", name = "offender_details_request", position = 5)
    @JsonProperty("offender_details_request")
    private String eventData;
}
