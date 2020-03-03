package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderPendingDeletionEventPusherTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private AmazonSQS client;

    private OffenderPendingDeletionEventPusher eventPusher;

    @BeforeEach
    void setUp() {
        eventPusher = new OffenderPendingDeletionAwsEventPusher(client, "queue.url", OBJECT_MAPPER);
    }

    @Test
    void sendEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.sendPendingDeletionEvent(OffenderPendingDeletionEvent.builder().offenderIdDisplay("offender1").build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"offenders\":[]}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION");
    }

    @Test
    void sendEventPropagatesException() {
        when(client.sendMessage(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> eventPusher.sendPendingDeletionEvent(
                OffenderPendingDeletionEvent.builder().offenderIdDisplay("offender1").build()))
                .isInstanceOf(RuntimeException.class);
    }
}