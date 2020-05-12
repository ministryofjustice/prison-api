package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.response.sqs.provider}')")
public class OffenderDeletionAwsEventPusher implements OffenderDeletionEventPusher {

    private final ObjectMapper objectMapper;
    private final AmazonSQS sqsClient;
    private final String queueUrl;

    public OffenderDeletionAwsEventPusher(
            @Autowired @Qualifier("dataComplianceResponseSqsClient") final AmazonSQS sqsClient,
            @Value("${data.compliance.response.sqs.queue.url}") final String queueUrl,
            final ObjectMapper objectMapper) {

        log.info("Configured to push offender pending deletion events to SQS queue: {}", queueUrl);

        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void sendPendingDeletionEvent(final OffenderPendingDeletionEvent event) {

        log.trace("Sending referral of offender pending deletion: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generatePendingDeletionRequest(event));
    }

    @Override
    public void sendReferralCompleteEvent(final OffenderPendingDeletionReferralCompleteEvent event) {

        log.trace("Sending process completed event for request: {}", event.getBatchId());

        sqsClient.sendMessage(generateReferralCompleteRequest(event));
    }

    @Override
    public void sendDeletionCompleteEvent(final OffenderDeletionCompleteEvent event) {

        log.trace("Sending offender deletion complete event: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateDeletionCompleteRequest(event));
    }

    private SendMessageRequest generatePendingDeletionRequest(final OffenderPendingDeletionEvent event) {
        return generateRequest("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION", event);
    }

    private SendMessageRequest generateReferralCompleteRequest(final OffenderPendingDeletionReferralCompleteEvent event) {
        return generateRequest("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION-REFERRAL-COMPLETE", event);
    }

    private SendMessageRequest generateDeletionCompleteRequest(final OffenderDeletionCompleteEvent event) {
        return generateRequest("DATA_COMPLIANCE_OFFENDER-DELETION-COMPLETE", event);
    }

    private SendMessageRequest generateRequest(final String eventType, final Object messageBody) {
        return new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageAttributes(Map.of(
                        "eventType", stringAttribute(eventType),
                        "contentType", stringAttribute("application/json;charset=UTF-8")))
                .withMessageBody(toJson(messageBody));
    }

    private MessageAttributeValue stringAttribute(final String value) {
        return new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(value);
    }

    private String toJson(final Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
