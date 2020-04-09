package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionGrantedEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDeletionService;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.inbound.deletion.sqs.provider}')")
public class OffenderDeletionEventListener {

    private static final String EXPECTED_EVENT_TYPE = "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED";

    private final OffenderDeletionService offenderDeletionService;
    private final ObjectMapper objectMapper;

    public OffenderDeletionEventListener(final OffenderDeletionService offenderDeletionService,
                                         final ObjectMapper objectMapper) {

        log.info("Configured to listen to Offender Deletion events");

        this.offenderDeletionService = offenderDeletionService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${data.compliance.inbound.deletion.sqs.queue.name}")
    public void handleOffenderDeletionEvent(final Message<String> message) {

        log.debug("Handling incoming offender deletion request: {}", message);

        checkEventType(message.getHeaders());

        final var event = parseOffenderDeletionEvent(message.getPayload());

        offenderDeletionService.deleteOffender(event.getOffenderIdDisplay(), event.getReferralId());
    }

    private void checkEventType(final MessageHeaders messageHeaders) {

        final var eventType = messageHeaders.get("eventType");

        checkState(EXPECTED_EVENT_TYPE.equals(eventType),
                "Unexpected message event type: '%s', expecting: '%s'", eventType, EXPECTED_EVENT_TYPE);
    }

    private OffenderDeletionGrantedEvent parseOffenderDeletionEvent(final String requestJson) {
        try {
            final var event = objectMapper.readValue(requestJson, OffenderDeletionGrantedEvent.class);

            checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", requestJson);
            checkNotNull(event.getReferralId(), "No referral ID specified in request: %s", requestJson);

            return event;
        } catch (final IOException e) {
            throw new RuntimeException("Failed to parse request: " + requestJson, e);
        }
    }
}
