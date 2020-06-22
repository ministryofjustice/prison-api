package uk.gov.justice.hmpps.nomis.datacompliance.health;

import com.amazonaws.services.sqs.AmazonSQS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.request.sqs.provider}')")
public class DataComplianceRequestQueueHealth extends QueueHealth {

    public DataComplianceRequestQueueHealth(
            @Autowired @Qualifier("dataComplianceRequestSqsClient") final AmazonSQS awsSqsClient,
            @Autowired @Qualifier("dataComplianceRequestSqsDlqClient") final AmazonSQS awsSqsDlqClient,
            @Value("${data.compliance.request.sqs.queue.name}") final String queueName,
            @Value("${data.compliance.request.sqs.dlq.name}") final String dlqName) {
        super(awsSqsClient, awsSqsDlqClient, queueName, dlqName);
    }
}
