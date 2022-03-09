package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Schema(description = "Visit Details")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Visit {

    private final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Schema(description = "Id", name = "id", example = "123456")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Slot", name = "slot", example = "2019-01-01T13:30/16:00")
    @JsonProperty("slot")
    private String slot;

    public Visit(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.slot = startTime.format(DATE_TIME_FORMAT) + "/" + endTime.format(TIME_FORMAT);
    }
}
