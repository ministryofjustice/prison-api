package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.deletion.complete;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.sns.provider}')")
public class OffenderDeletionCompleteAwsEventPusher implements OffenderDeletionCompleteEventPusher {

    private final AmazonSNS amazonSns;
    private final String topicArn;
    private final ObjectMapper objectMapper;

    public OffenderDeletionCompleteAwsEventPusher(@Qualifier("awsSnsClient") final AmazonSNS amazonSns,
                                                  @Value("${data.compliance.sns.topic.arn}") final String topicArn,
                                                  final ObjectMapper objectMapper) {
        this.topicArn = topicArn;
        this.amazonSns = amazonSns;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendEvent(final OffenderDeletionCompleteEvent event) {
        amazonSns.publish(generateRequest(event));
    }

    private PublishRequest generateRequest(final OffenderDeletionCompleteEvent event) {
        return new PublishRequest()
                .withTopicArn(topicArn)
                .withMessageAttributes(Map.of(
                        "eventType", stringAttribute("DATA_COMPLIANCE_DELETE-OFFENDER"),
                        "contentType", stringAttribute("text/plain;charset=UTF-8")))
                .withMessage(toJson(event));
    }

    private MessageAttributeValue stringAttribute(final String value) {
        return new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(value);
    }

    private String toJson(final OffenderDeletionCompleteEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
