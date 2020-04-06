package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDataComplianceService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffenderDeletionEventListenerTest {

    @Mock
    private OffenderDataComplianceService offenderDataComplianceService;

    private ObjectMapper objectMapper;
    private OffenderDeletionEventListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new OffenderDeletionEventListener(offenderDataComplianceService, objectMapper);
    }

    @Test
    void handleOffenderDeletionEvent() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\"}",
                Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED"));

        verify(offenderDataComplianceService).deleteOffender("A1234AA");
    }

    @Test
    void handleOffenderDeletionEventThrowsIfMessageAttributesNotPresent() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected message event type: 'null', expecting: 'DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED'");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfEventTypeUnexpected() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "UNEXPECTED!")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected message event type: 'UNEXPECTED!', expecting: 'DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED'");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfMessageNotPresent() {
        assertThatThrownBy(() -> handleMessage(null, Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("argument \"content\" is null");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfMessageUnparsable() {

        assertThatThrownBy(() -> handleMessage("BAD MESSAGE!", Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse request");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\"}", Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDataComplianceService);
    }

    private void handleMessage(final String payload, final Map<String, Object> headers) {
        listener.handleOffenderDeletionEvent(mockMessage(payload, headers));
    }

    @SuppressWarnings("unchecked")
    private Message<String> mockMessage(final String payload, final Map<String, Object> headers) {
        final var message = mock(Message.class);
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        lenient().when(message.getPayload()).thenReturn(payload);
        return message;
    }
}