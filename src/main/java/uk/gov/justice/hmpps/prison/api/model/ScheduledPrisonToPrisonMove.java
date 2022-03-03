package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "Represents the data for a scheduled prison to prison move.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ScheduledPrisonToPrisonMove {

    @Schema(description = "The identifier for the scheduled prison to prison move.", example = "123456789")
    private Long id;

    @Schema(description = "The date and start time of the move in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2020-02-28T14:40:00")
    private LocalDateTime scheduledMoveDateTime;

    @Schema(description = "The location of the prison to move from.", example = "PVI")
    private Agency fromPrisonLocation;

    @Schema(description = "The location of the prison to move to.", example = "LEI")
    private Agency toPrisonLocation;
}
