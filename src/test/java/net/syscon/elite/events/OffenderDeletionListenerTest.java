package net.syscon.elite.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.syscon.elite.service.OffenderDeletionService;
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
    private OffenderDeletionService offenderDeletionService;

    private ObjectMapper objectMapper;
    private OffenderDeletionListener listener;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        listener = new OffenderDeletionListener(offenderDeletionService, objectMapper);
    }

    @Test
    public void handleOffenderDeletionEvent() throws Exception {

        listener.handleOffenderDeletionEvent(getJson("offender-deletion-request.json"));

        verify(offenderDeletionService).deleteOffender("A1234AA");
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfMessageNotPresent() {
        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent("{}"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Request did not contain 'Message' key: {}");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfMessageUnparsable() {

        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(
                getJson("offender-deletion-request-bad-message.json")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to parse request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    public void handleOffenderDeletionEventThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> listener.handleOffenderDeletionEvent(
                getJson("offender-deletion-request-empty.json")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    private String getJson(final String filename) throws IOException {
        return IOUtils.toString(this.getClass().getResource(filename).openStream(), UTF_8);
    }
}