package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderPendingDeletionEventPusherTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private AmazonSNSClient client;

    private OffenderPendingDeletionEventPusher eventPusher;

    @BeforeEach
    void setUp() {
        eventPusher = new OffenderPendingDeletionAwsEventPusher(client, "topic.arn", OBJECT_MAPPER);
    }

    @Test
    void sendEvent() {

        final var request = ArgumentCaptor.forClass(PublishRequest.class);

        when(client.publish(request.capture()))
                .thenReturn(new PublishResult().withMessageId("message1"));

        eventPusher.sendEvent("offender1");

        assertThat(request.getValue().getMessage()).contains("offender1");
        assertThat(request.getValue().getTopicArn()).isEqualTo("topic.arn");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION");
    }

    @Test
    void sendEventPropagatesException() {
        when(client.publish(any())).thenThrow(RuntimeException.class);
        assertThatThrownBy(() -> eventPusher.sendEvent("offender1")).isInstanceOf(RuntimeException.class);
    }
}