package uk.gov.justice.hmpps.nomis.datacompliance.health;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Map;

import static com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessages;
import static com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessagesNotVisible;
import static uk.gov.justice.hmpps.nomis.datacompliance.health.QueueHealth.DlqStatus.*;
import static uk.gov.justice.hmpps.nomis.datacompliance.health.QueueHealth.QueueAttributes.*;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

@Slf4j
public abstract class QueueHealth implements HealthIndicator {

    @AllArgsConstructor
    enum DlqStatus {
        UP("UP"),
        NOT_ATTACHED("The queue does not have a dead letter queue attached"),
        NOT_FOUND("The queue does not exist"),
        NOT_AVAILABLE("The queue cannot be interrogated");

        final String description;
    }

    @AllArgsConstructor
    enum QueueAttributes {
        MESSAGES_ON_QUEUE(ApproximateNumberOfMessages.toString(), "MessagesOnQueue"),
        MESSAGES_IN_FLIGHT(ApproximateNumberOfMessagesNotVisible.toString(), "MessagesInFlight"),
        MESSAGES_ON_DLQ(ApproximateNumberOfMessages.toString(), "MessagesOnDLQ");

        final String awsName;
        final String healthName;
    }

    private final AmazonSQS awsSqsClient;
    private final AmazonSQS awsSqsDlqClient;
    private final String queueName;
    private final String dlqName;

    public QueueHealth(final AmazonSQS awsSqsClient,
                       final AmazonSQS awsSqsDlqClient,
                       final String queueName,
                       final String dlqName) {

        this.awsSqsClient = awsSqsClient;
        this.awsSqsDlqClient = awsSqsDlqClient;
        this.queueName = queueName;
        this.dlqName = dlqName;
    }

    @Override
    public Health health() {

        try {
            return queueHealth(getQueueAttributes(awsSqsClient, queueName));
        } catch (Exception e) {
            log.error("Unable to retrieve queue attributes for queue '{}' due to exception:", queueName, e);
            return down().withException(e).build();
        }
    }

    private Health queueHealth(final GetQueueAttributesResult attributes) {

        if (!attributes.getAttributes().containsKey("RedrivePolicy")) {
            log.error("Queue '{}' is missing a RedrivePolicy attribute indicating it does not have a dead letter queue", queueName);
            return down()
                    .withDetails(mainQueueDetails(attributes))
                    .withDetail("dlqStatus", NOT_ATTACHED.description)
                    .build();
        }

        try {
            return up()
                    .withDetails(mainQueueDetails(attributes))
                    .withDetails(dlqDetails())
                    .build();

        } catch (QueueDoesNotExistException e) {
            log.error("Unable to retrieve dead letter queue URL for queue '{}' due to exception:", queueName, e);
            return down(e)
                    .withDetails(mainQueueDetails(attributes))
                    .withDetail("dlqStatus", NOT_FOUND.description)
                    .build();

        } catch (Exception e){
            log.error("Unable to retrieve dead letter queue attributes for queue '{}' due to exception:", queueName, e);
            return down(e)
                    .withDetails(mainQueueDetails(attributes))
                    .withDetail("dlqStatus", NOT_AVAILABLE.description).build();
        }
    }

    private Map<String, String> mainQueueDetails(final GetQueueAttributesResult attributes) {
        return Map.of(
                MESSAGES_ON_QUEUE.healthName, attributes.getAttributes().get(MESSAGES_ON_QUEUE.awsName),
                MESSAGES_IN_FLIGHT.healthName, attributes.getAttributes().get(MESSAGES_IN_FLIGHT.awsName));
    }

    private Map<String, String> dlqDetails() {

        final var messagesOnDlq = getQueueAttributes(awsSqsDlqClient, dlqName)
                .getAttributes()
                .get(MESSAGES_ON_DLQ.awsName);

        return Map.of(
                "dlqStatus", UP.description,
                MESSAGES_ON_DLQ.healthName, messagesOnDlq);
    }

    private GetQueueAttributesResult getQueueAttributes(final AmazonSQS client, final String queueName) {
        final var url = client.getQueueUrl(queueName);
        return client.getQueueAttributes(getQueueAttributesRequest(url));
    }

    private GetQueueAttributesRequest getQueueAttributesRequest(final GetQueueUrlResult url) {
        return new GetQueueAttributesRequest(url.getQueueUrl()).withAttributeNames(QueueAttributeName.All);
    }
}
