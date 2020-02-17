package uk.gov.justice.hmpps.nomis.datacompliance.events.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SqsEvent {

    @JsonProperty("Message")
    private String message;

    @JsonProperty("MessageAttributes")
    private MessageAttributes messageAttributes;

    public String getEventType() {

        requireNonNull(messageAttributes, "Event has no attributes");
        requireNonNull(messageAttributes.getEventType(), "Message attributes has no eventType");

        return messageAttributes.getEventType().getValue();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MessageAttributes {
        @JsonProperty("eventType") private Attribute eventType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Attribute {
        @JsonProperty("Value") private String value;
    }
}
