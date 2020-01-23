package net.syscon.elite.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.syscon.elite.service.OffenderDataComplianceService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OffenderDeletionListenerTest {

    @Mock
    private OffenderDataComplianceService offenderDataComplianceService;

    private ObjectMapper objectMapper;
    private OffenderDeletionListener listener;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        listener = new OffenderDeletionListener(offenderDataComplianceService, objectMapper);
    }

    @Test
    public void handleOffenderDeletionEvent() throws Exception {

        listener.handleOffenderDeletionEvent(getJson("offender-deletion-request.json"));

        verify(offenderDataComplianceService).deleteOffender("A1234AA");
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfMessageAttributesNotPresent() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(getJson("offender-deletion-request-no-attributes.json")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Event has no attributes");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfEventTypeUnexpected() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(getJson("offender-deletion-request-bad-event-type.json")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected message event type: UNEXPECTED!");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfMessageNotPresent() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(getJson("offender-deletion-request-no-message.json")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("argument \"content\" is null");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfMessageUnparsable() {

        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(
                getJson("offender-deletion-request-bad-message.json")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse request");

        verifyNoInteractions(offenderDataComplianceService);
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfOffenderIdDisplayEmpty() {

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