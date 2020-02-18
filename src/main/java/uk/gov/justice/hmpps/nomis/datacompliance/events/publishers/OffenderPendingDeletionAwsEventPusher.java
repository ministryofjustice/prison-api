package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.outbound.referral.sqs.provider}')")
public class OffenderPendingDeletionAwsEventPusher implements OffenderPendingDeletionEventPusher {

    private final ObjectMapper objectMapper;
    private final AmazonSQS sqsClient;
    private final String queueUrl;

    public OffenderPendingDeletionAwsEventPusher(
            @Autowired @Qualifier("outboundReferralSqsClient") final AmazonSQS sqsClient,
            @Value("${data.compliance.outbound.referral.sqs.queue.url}") final String queueUrl,
            final ObjectMapper objectMapper) {

        log.info("Configured to push offender pending deletion events to SQS queue: {}", queueUrl);

        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void sendEvent(final String offenderNo) {

        log.debug("Sending referral of offender pending deletion: {}", offenderNo);

        sqsClient.sendMessage(generateRequest(offenderNo));
    }

    private SendMessageRequest generateRequest(final String offenderNo) {
        return new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageAttributes(Map.of(
                        "eventType", stringAttribute("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION"),
                        "contentType", stringAttribute("text/plain;charset=UTF-8")))
                .withMessageBody(toJson(new OffenderPendingDeletionEvent(offenderNo)));
    }

    private MessageAttributeValue stringAttribute(final String value) {
        return new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(value);
    }

    private String toJson(final OffenderPendingDeletionEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
