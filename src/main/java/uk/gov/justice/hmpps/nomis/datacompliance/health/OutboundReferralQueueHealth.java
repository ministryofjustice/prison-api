package uk.gov.justice.hmpps.nomis.datacompliance.health;

import com.amazonaws.services.sqs.AmazonSQS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.outbound.referral.sqs.provider}')")
public class OutboundReferralQueueHealth extends QueueHealth {

    public OutboundReferralQueueHealth(
            @Autowired @Qualifier("outboundReferralSqsClient") final AmazonSQS awsSqsClient,
            @Autowired @Qualifier("outboundReferralSqsDlqClient") final AmazonSQS awsSqsDlqClient,
            @Value("${data.compliance.outbound.referral.sqs.queue.name}") final String queueName,
            @Value("${data.compliance.outbound.referral.sqs.dlq.name}") final String dlqName) {
        super(awsSqsClient, awsSqsDlqClient, queueName, dlqName);
    }
}
