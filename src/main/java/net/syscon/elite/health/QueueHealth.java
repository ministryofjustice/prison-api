package net.syscon.elite.health;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessages;
import static com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessagesNotVisible;
import static net.syscon.elite.health.QueueHealth.DlqStatus.*;
import static net.syscon.elite.health.QueueHealth.QueueAttributes.*;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

@Slf4j
@Component
@ConditionalOnExpression("'${offender.deletion.sqs.provider}'.equals('aws') or '${offender.deletion.sqs.provider}'.equals('localstack')")
public class QueueHealth implements HealthIndicator {

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

    public QueueHealth(@Autowired @Qualifier("awsSqsClient") final AmazonSQS awsSqsClient,
                       @Autowired @Qualifier("awsSqsDlqClient") final AmazonSQS awsSqsDlqClient,
                       @Value("${offender.deletion.sqs.queue.name}") final String queueName,
                       @Value("${offender.deletion.sqs.dlq.name}") final String dlqName) {

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
            return mainQueueHealth(attributes)
                    .down().withDetail("dlqStatus", NOT_ATTACHED.description).build();
        }

        try {
            return mainQueueHealth(attributes)
                    .withDetails(dlqDetails())
                    .build();

        } catch (QueueDoesNotExistException e) {
            log.error("Unable to retrieve dead letter queue URL for queue '{}' due to exception:", queueName, e);
            return unhealthyDlq(attributes, e)
                    .withDetail("dlqStatus", NOT_FOUND.description).build();
        } catch (Exception e){
            log.error("Unable to retrieve dead letter queue attributes for queue '{}' due to exception:", queueName, e);
            return unhealthyDlq(attributes, e)
                    .withDetail("dlqStatus", NOT_AVAILABLE.description).build();
        }
    }

    private Builder unhealthyDlq(final GetQueueAttributesResult attributes, final Exception e) {
        return mainQueueHealth(attributes).down(e);
    }

    private Builder mainQueueHealth(final GetQueueAttributesResult attributes) {
        final var details = Map.of(
                MESSAGES_ON_QUEUE.healthName, attributes.getAttributes().get(MESSAGES_ON_QUEUE.awsName),
                MESSAGES_IN_FLIGHT.healthName, attributes.getAttributes().get(MESSAGES_IN_FLIGHT.awsName));

        return up().withDetails(details);
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
