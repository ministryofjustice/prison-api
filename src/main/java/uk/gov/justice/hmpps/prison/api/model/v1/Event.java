package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;
import uk.gov.justice.hmpps.prison.api.model.v1.Event.EventSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender Event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonSerialize(using = EventSerializer.class)
public class Event {

    @Schema(description = "Type of event", requiredMode = REQUIRED, example = "IEP_CHANGED")
    private String type;
    @Schema(description = "Unique indentifier for event", requiredMode = REQUIRED, example = "21")
    private Long id;
    @Schema(name = "noms_id", description = "Offender Noms Id", example = "A1417AE", requiredMode = REQUIRED)
    private String nomsId;
    @Schema(name = "prison_id", description = "Prison ID", example = "BMI", requiredMode = REQUIRED)
    private String prisonId;
    @Schema(name = "timestamp", description = "Date and time the event occurred", example = "2016-10-21 15:55:06.284", requiredMode = REQUIRED)
    private LocalDateTime timestamp;

    private String eventData;

    public static class EventSerializer extends ValueSerializer<Event> {
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        public void serialize(Event event, JsonGenerator jgen, SerializationContext ctxt) throws JacksonException {
            jgen.writeStartObject();
            jgen.writeStringProperty("type", event.getType());
            jgen.writeNumberProperty("id", event.getId());
            jgen.writeStringProperty("noms_id", event.getNomsId());
            jgen.writeStringProperty("prison_id", event.getPrisonId());
            jgen.writeStringProperty("timestamp", event.getTimestamp().format(DATE_TIME_FORMATTER));
            // Write value as raw data, since it's already JSON text
            jgen.writeName(event.getType().toLowerCase());
            jgen.writeRawValue(event.getEventData());
            jgen.writeEndObject();
        }
    }
}
