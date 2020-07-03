package uk.gov.justice.hmpps.nomis.prison.datacompliance.health;

import com.amazonaws.services.sqs.AmazonSQS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.response.sqs.provider}')")
public class DataComplianceResponseQueueHealth extends QueueHealth {

    public DataComplianceResponseQueueHealth(
            @Autowired @Qualifier("dataComplianceResponseSqsClient") final AmazonSQS awsSqsClient,
            @Autowired @Qualifier("dataComplianceResponseSqsDlqClient") final AmazonSQS awsSqsDlqClient,
            @Value("${data.compliance.response.sqs.queue.name}") final String queueName,
            @Value("${data.compliance.response.sqs.dlq.name}") final String dlqName) {
        super(awsSqsClient, awsSqsDlqClient, queueName, dlqName);
    }
}
