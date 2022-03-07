package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ApiModel(description = "Visit slots Details ")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class VisitSlotCapacity {

    @ApiModelProperty(value = "Time", name = "time", example = "2019-01-01T13:30/16:00", position = 1)
    @JsonProperty("time")
    private String time;

    @ApiModelProperty(value = "Capacity", name = "capacity", example = "402", position = 2)
    @JsonProperty("capacity")
    private Long capacity;

    @ApiModelProperty(value = "Max Groups", name = "max_groups", example = "999", position = 3)
    @JsonProperty("max_groups")
    private Long maxGroups;

    @ApiModelProperty(value = "Max Adults", name = "max_adults", example = "999", position = 4)
    @JsonProperty("max_adults")
    private Long maxAdults;

    @ApiModelProperty(value = "Groups Booked", name = "groups_booked", example = "5", position = 5)
    @JsonProperty("groups_booked")
    private Long groupsBooked;

    @ApiModelProperty(value = "Visitors Booked", name = "visitors_booked", example = "6", position = 6)
    @JsonProperty("visitors_booked")
    private Long visitorsBooked;

    @ApiModelProperty(value = "Adults Booked", name = "adults_booked", example = "7", position = 7)
    @JsonProperty("adults_booked")
    private Long adultsBooked;


}
