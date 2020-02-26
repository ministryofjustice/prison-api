package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.deletion.complete;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent.OffenderWithBookings;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderDeletionCompleteEventPusherTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private AmazonSNS client;

    private OffenderDeletionCompleteEventPusher eventPusher;

    @BeforeEach
    void setUp() {
        eventPusher = new OffenderDeletionCompleteAwsEventPusher(client, "topic.arn", OBJECT_MAPPER);
    }

    @Test
    void sendEvent() throws Exception {

        final var request = ArgumentCaptor.forClass(PublishRequest.class);
        when(client.publish(request.capture()))
                .thenReturn(new PublishResult().withMessageId("messageId"));

        eventPusher.sendEvent(deletionCompleteEvent());

        assertThat(request.getValue().getMessage()).isEqualToIgnoringWhitespace(getJson("delete-offender-event.json"));
        assertThat(request.getValue().getTopicArn()).isEqualTo("topic.arn");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_DELETE-OFFENDER");
    }

    @Test
    void sendEventPropagatesException() {
        when(client.publish(any())).thenThrow(RuntimeException.class);
        assertThatThrownBy(() -> eventPusher.sendEvent(any())).isInstanceOf(RuntimeException.class);
    }

    private OffenderDeletionCompleteEvent deletionCompleteEvent() {
        return OffenderDeletionCompleteEvent.builder()
                .offenderIdDisplay("offender1")
                .offender(OffenderWithBookings.builder()
                        .offenderId(1L)
                        .booking(new Booking(11L))
                        .booking(new Booking(12L))
                        .build())
                .offender(OffenderWithBookings.builder()
                        .offenderId(2L)
                        .booking(new Booking(21L))
                        .booking(new Booking(22L))
                        .build())
                .build();
    }

    private String getJson(final String filename) throws IOException {
        return IOUtils.toString(this.getClass().getResource(filename).openStream(), UTF_8);
    }
}

