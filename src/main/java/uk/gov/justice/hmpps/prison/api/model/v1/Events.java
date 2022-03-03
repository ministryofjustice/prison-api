package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Schema(description = "Events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Events {
    @Schema(description = "Events")
    private List<Event> events;
}
