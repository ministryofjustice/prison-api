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
    private LocalDateTime entryDate;
    private LocalDateTime releaseDate;

    @Default
    private List<MovementDate> movementDates = new ArrayList<>();

    @JsonIgnore
    public Optional<MovementDate> getLastMovement() {
        if (movementDates.isEmpty() ) return Optional.empty();
        return Optional.of(movementDates.get(movementDates.size() - 1));
    }


}
