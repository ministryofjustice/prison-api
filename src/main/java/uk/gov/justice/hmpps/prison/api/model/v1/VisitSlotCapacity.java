package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Visit slots Details ")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class VisitSlotCapacity {

    @Schema(description = "Time", name = "time", example = "2019-01-01T13:30/16:00")
    @JsonProperty("time")
    private String time;

    @Schema(description = "Capacity", name = "capacity", example = "402")
    @JsonProperty("capacity")
    private Long capacity;

    @Schema(description = "Max Groups", name = "max_groups", example = "999")
    @JsonProperty("max_groups")
    private Long maxGroups;

    @Schema(description = "Max Adults", name = "max_adults", example = "999")
    @JsonProperty("max_adults")
    private Long maxAdults;

    @Schema(description = "Groups Booked", name = "groups_booked", example = "5")
    @JsonProperty("groups_booked")
    private Long groupsBooked;

    @Schema(description = "Visitors Booked", name = "visitors_booked", example = "6")
    @JsonProperty("visitors_booked")
    private Long visitorsBooked;

    @Schema(description = "Adults Booked", name = "adults_booked", example = "7")
    @JsonProperty("adults_booked")
    private Long adultsBooked;


}
