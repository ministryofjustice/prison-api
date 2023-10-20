package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "A period of time in prison")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class PrisonPeriod {

    private String bookNumber;
    private Long bookingId;

    @Schema(description = "Date they first entered prison in this booking", requiredMode = REQUIRED)
    private LocalDateTime entryDate;
    @Schema(description = "Date they were last released from prison in this booking if they have been released", requiredMode = NOT_REQUIRED)
    private LocalDateTime releaseDate;

    @Default
    @Schema(description = "List of significant period of time when in prison. The time between these periods means they person was out of prison (but not including court)", requiredMode = REQUIRED)
    private List<SignificantMovement> movementDates = new ArrayList<>();
    @Default
    @Schema(description = "List of prisons the person was detained during this booking period", requiredMode = REQUIRED)
    private List<String> prisons = new ArrayList<>();

    @JsonIgnore
    public Optional<SignificantMovement> getLastMovement() {
        if (movementDates.isEmpty() ) return Optional.empty();
        return Optional.of(movementDates.get(movementDates.size() - 1));
    }


}
