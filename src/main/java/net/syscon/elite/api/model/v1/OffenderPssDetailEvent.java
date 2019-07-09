package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

@ApiModel(description = "OffenderPssDetailEvent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "eventType", "nomsId"})
@JsonAutoDetect(
        fieldVisibility = Visibility.ANY,
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"type", "id", "noms_id", "prison_id", "timestamp", "offender_details_request"})
public class OffenderPssDetailEvent {

    @ApiModelProperty(value = "Event type", name = "type", example = "offender_details_request", position = 1)
    @JsonProperty("type")
    private String eventType;

    @ApiModelProperty(value = "Event identifier (always 0)", name = "id", example = "0", position = 2)
    private Long id;

    @ApiModelProperty(value = "Event date and time", name = "timestamp", example = "2019-04-23 14:23:00.000", position = 3)
    @JsonProperty("timestamp")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime eventTimeStamp;

    @ApiModelProperty(value = "Prison identifier", name = "prison_id", example = "MDI", position = 4)
    @JsonProperty("prison_id")
    private String prisonId;

    @ApiModelProperty(value = "Noms identifier", name = "noms_id", example = "A1404AE", position = 5)
    @JsonProperty("noms_id")
    private String nomsId;

    @ApiModelProperty(value = "Offender details", name = "offender_details_request", position = 6)
    @JsonProperty("offender_details_request")
    private PssOffenderDetail pssDetail;
}
