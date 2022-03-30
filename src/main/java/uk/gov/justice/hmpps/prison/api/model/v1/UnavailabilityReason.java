package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Date Unavailability Reasons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"external_movement", "existing_visits", "out_of_vo", "banned"})
public class UnavailabilityReason {

    @Schema(description = "External Movement", name = "external_movement", example = "true")
    @JsonProperty("external_movement")
    private boolean externalMovement;

    @Schema(description = "Existing Visits", name = "existing_visits")
    @JsonProperty("existing_visits")
    private List<Visit> existingVisits = new ArrayList<>();

    @Schema(description = "Out of Vo", name = "out_of_vo", example = "true")
    @JsonProperty("out_of_vo")
    private boolean outOfVo;

    @Schema(description = "Banned", name = "banned", example = "true")
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
