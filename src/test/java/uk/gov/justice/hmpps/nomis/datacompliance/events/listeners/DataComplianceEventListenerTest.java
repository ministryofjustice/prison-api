package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataDuplicateService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.FreeTextSearchService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDeletionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataComplianceEventListenerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private DataDuplicateService dataDuplicateService;

    @Mock
    private OffenderDeletionService offenderDeletionService;

    @Mock
    private FreeTextSearchService freeTextSearchService;

    private DataComplianceEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DataComplianceEventListener(
                dataDuplicateService,
                offenderDeletionService,
                freeTextSearchService,
                MAPPER);
    }

    @Test
    void handleOffenderDeletionEvent() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"referralId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED"));

        verify(offenderDeletionService).deleteOffender("A1234AA", 123L);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\",\"referralId\":123}", Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfReferralIdNull() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No referral ID specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleDuplicateIdCheck() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK"));

        verify(dataDuplicateService).checkForDuplicateIds("A1234AA", 123L);
    }

    @Test
    void handleDuplicateIdCheckThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\",\"retentionCheckId\":123}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleDuplicateIdCheckThrowsIfRetentionCheckIdNull() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No retention check ID specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleDuplicateDataCheck() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK"));

        verify(dataDuplicateService).checkForDataDuplicates("A1234AA", 123L);
    }

    @Test
    void handleDuplicateDataCheckThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\",\"retentionCheckId\":123}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(dataDuplicateService);
    }

    @Test
    void handleDuplicateDataCheckThrowsIfRetentionCheckIdNull() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No retention check ID specified in request");

        verifyNoInteractions(dataDuplicateService);
    }

    @Test
    void handleFreeTextCheck() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"regex\":\"^(some|regex)$\"}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK"));

        verify(freeTextSearchService).checkForMatchingContent("A1234AA", 123L, "^(some|regex)$");
    }

    @Test
    void handleFreeTextCheckThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"\",\"retentionCheckId\":123,\"regex\":\"^(some|regex)$\"}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleFreeTextCheckThrowsIfRetentionCheckIdNull() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"regex\":\"^(some|regex)$\"}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No retention check ID specified in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleFreeTextCheckThrowsIfRegexEmpty() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"regex\":\"\"}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No regex specified in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleEventThrowsIfMessageAttributesNotPresent() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Message event type not found");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfEventTypeUnexpected() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "UNEXPECTED!")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unexpected message event type: 'UNEXPECTED!'");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfMessageNotPresent() {
        assertThatThrownBy(() -> handleMessage(null, Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("argument \"content\" is null");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfMessageUnparsable() {

        assertThatThrownBy(() -> handleMessage("BAD MESSAGE!", Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse request");

        verifyNoInteractions(offenderDeletionService);
    }


    private void handleMessage(final String payload, final Map<String, Object> headers) {
        listener.handleEvent(mockMessage(payload, headers));
    }

    @SuppressWarnings("unchecked")
    private Message<String> mockMessage(final String payload, final Map<String, Object> headers) {
        final var message = mock(Message.class);
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        lenient().when(message.getPayload()).thenReturn(payload);
        return message;
    }
}