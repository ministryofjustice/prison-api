package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDataComplianceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.inbound.deletion.sqs.provider}')")
public class OffenderDeletionListener {

    private static final String EXPECTED_EVENT_TYPE = "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED";

    private final OffenderDataComplianceService offenderDataComplianceService;
    private final ObjectMapper objectMapper;

    public OffenderDeletionListener(final OffenderDataComplianceService offenderDataComplianceService,
                                    final ObjectMapper objectMapper) {

        log.info("Configured to listen to Offender Deletion events");

        this.offenderDataComplianceService = offenderDataComplianceService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${data.compliance.inbound.deletion.sqs.queue.name}")
    public void handleOffenderDeletionEvent(final Message<String> message) {

        log.debug("Handling incoming offender deletion request: {}", message);

        checkEventType(message.getHeaders());

        offenderDataComplianceService.deleteOffender(
                getOffenderIdDisplay(message.getPayload()));
    }

    private void checkEventType(final MessageHeaders messageHeaders) {

        final var eventType = messageHeaders.get("eventType");

        checkState(EXPECTED_EVENT_TYPE.equals(eventType),
                "Unexpected message event type: '%s', expecting: '%s'", eventType, EXPECTED_EVENT_TYPE);
    }

    private String getOffenderIdDisplay(final String messageBody) {

        final OffenderDeletionEvent event = parseOffenderDeletionEvent(messageBody);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", messageBody);

        return event.getOffenderIdDisplay();
    }

    private OffenderDeletionEvent parseOffenderDeletionEvent(final String requestJson) {
        try {
            return objectMapper.readValue(requestJson, OffenderDeletionEvent.class);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse request: " + requestJson, e);
        }
    }
}
