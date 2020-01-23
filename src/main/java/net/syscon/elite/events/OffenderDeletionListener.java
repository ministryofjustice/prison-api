package net.syscon.elite.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.events.dto.OffenderDeletionEvent;
import net.syscon.elite.events.dto.SqsEvent;
import net.syscon.elite.service.OffenderDataComplianceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnExpression("'${offender.deletion.sqs.provider}'.equals('aws') or '${offender.deletion.sqs.provider}'.equals('localstack')")
public class OffenderDeletionListener {

    private static final String EXPECTED_EVENT_TYPE = "DATA_COMPLIANCE_DELETE-OFFENDER";

    private final OffenderDataComplianceService offenderDataComplianceService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${offender.deletion.sqs.queue.name}")
    public void handleOffenderDeletionEvent(final String requestJson) {

        log.debug("Handling incoming offender deletion request: {}", requestJson);

        offenderDataComplianceService.deleteOffender(
                getOffenderIdDisplay(requestJson));
    }

    private String getOffenderIdDisplay(final String requestJson) {

        final OffenderDeletionEvent event = parseOffenderDeletionEvent(requestJson);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", requestJson);

        return event.getOffenderIdDisplay();
    }

    private OffenderDeletionEvent parseOffenderDeletionEvent(final String requestJson) {
        try {
            final SqsEvent message = objectMapper.readValue(requestJson, SqsEvent.class);

            checkState(EXPECTED_EVENT_TYPE.equals(message.getEventType()),
                    "Unexpected message event type: '%s', expecting: '%s'", message.getEventType(), EXPECTED_EVENT_TYPE);

            return objectMapper.readValue(message.getMessage(), OffenderDeletionEvent.class);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse request: " + requestJson, e);
        }
    }
}
