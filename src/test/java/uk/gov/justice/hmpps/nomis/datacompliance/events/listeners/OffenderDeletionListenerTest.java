package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDataComplianceService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffenderDeletionListenerTest {

    @Mock
    private OffenderDataComplianceService offenderDataComplianceService;

    private ObjectMapper objectMapper;
    private OffenderDeletionListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new OffenderDeletionListener(offenderDataComplianceService, objectMapper);
    }

    @Test
    void handleOffenderDeletionEvent() throws Exception {

        listener.handleOffenderDeletionEvent(getJson("offender-deletion-request.json"));

        verify(offenderDataComplianceService).deleteOffender("A1234AA");
    }

    @Test
    void handleOffenderDeletionEventThrowsIfMessageAttributesNotPresent() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(getJson("offender-deletion-request-no-attributes.json")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Event has no attributes");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfEventTypeUnexpected() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(getJson("offender-deletion-request-bad-event-type.json")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected message event type: 'UNEXPECTED!', expecting: 'DATA_COMPLIANCE_DELETE-OFFENDER'");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfMessageNotPresent() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(getJson("offender-deletion-request-no-message.json")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("argument \"content\" is null");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfMessageUnparsable() {

        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(
                getJson("offender-deletion-request-bad-message.json")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse request");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(
                getJson("offender-deletion-request-empty.json")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDataComplianceService);
    }

    private String getJson(final String filename) throws IOException {
        return IOUtils.toString(this.getClass().getResource(filename).openStream(), UTF_8);
    }
}