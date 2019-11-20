package net.syscon.elite.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.events.dto.OffenderDeletionEvent;
import net.syscon.elite.service.OffenderDeletionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(name = "offender.deletion.sqs.provider")
public class OffenderDeletionListener {

    private final OffenderDeletionService offenderDeletionService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${offender.deletion.sqs.queue.name}")
    public void handleOffenderDeletionEvent(final String requestJson) {

        log.debug("Handling incoming offender deletion request: {}", requestJson);

        offenderDeletionService.deleteOffender(
                getOffenderIdDisplay(requestJson));
    }

    private String getOffenderIdDisplay(final String requestJson) {

        final OffenderDeletionEvent event = parseOffenderDeletionEvent(requestJson);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", requestJson);

        return event.getOffenderIdDisplay();
    }

    private OffenderDeletionEvent parseOffenderDeletionEvent(final String requestJson) {
        try {
            final Map<String, Object> message = objectMapper.readValue(requestJson, new TypeReference<>() {});

            requireNonNull(message, "Could not parse request into map: " + requestJson);
            requireNonNull(message.get("Message"), "Request did not contain 'Message' key: " + requestJson);

            return objectMapper.readValue(message.get("Message").toString(), OffenderDeletionEvent.class);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse request", e);
        }
    }
}
