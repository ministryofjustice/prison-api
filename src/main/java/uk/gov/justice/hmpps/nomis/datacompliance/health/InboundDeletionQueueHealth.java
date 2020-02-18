package uk.gov.justice.hmpps.nomis.datacompliance.health;

import com.amazonaws.services.sqs.AmazonSQS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.inbound.deletion.sqs.provider}')")
public class InboundDeletionQueueHealth extends QueueHealth {

    public InboundDeletionQueueHealth(
            @Autowired @Qualifier("inboundDeletionSqsClient") final AmazonSQS awsSqsClient,
            @Autowired @Qualifier("inboundDeletionSqsDlqClient") final AmazonSQS awsSqsDlqClient,
            @Value("${data.compliance.inbound.deletion.sqs.queue.name}") final String queueName,
            @Value("${data.compliance.inbound.deletion.sqs.dlq.name}") final String dlqName) {
        super(awsSqsClient, awsSqsDlqClient, queueName, dlqName);
    }
}
