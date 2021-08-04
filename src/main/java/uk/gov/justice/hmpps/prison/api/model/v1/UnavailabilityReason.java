package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Date Unavailability Reasons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"external_movement", "existing_visits", "out_of_vo", "banned"})
public class UnavailabilityReason {

    @ApiModelProperty(value = "External Movement", name = "external_movement", example = "true", position = 1)
    @JsonProperty("external_movement")
    private boolean externalMovement;

    @ApiModelProperty(value = "Existing Visits", name = "existing_visits", position = 2)
    @JsonProperty("existing_visits")
    private List<Visit> existingVisits = new ArrayList<>();

    @ApiModelProperty(value = "Out of Vo", name = "out_of_vo", example = "true", position = 3)
    @JsonProperty("out_of_vo")
    private boolean outOfVo;

    @ApiModelProperty(value = "Banned", name = "banned", example = "true", position = 4)
    @JsonProperty("banned")
    private boolean banned;

    public UnavailabilityReason update(UnavailabilityReasonSP r) {
        switch (r.getReason()) {
            case "COURT" -> setExternalMovement(true);
            case "BAN" -> setBanned(true);
            case "VO" -> setOutOfVo(true);
            case "VISIT" -> getExistingVisits().add(new Visit(r.getVisitId(), r.getSlotStart(), r.getSlotEnd()));
        }
        return this;
    }
}
