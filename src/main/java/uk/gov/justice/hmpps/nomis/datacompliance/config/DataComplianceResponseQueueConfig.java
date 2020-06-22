package uk.gov.justice.hmpps.nomis.datacompliance.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@Slf4j
@EnableJms
@Configuration
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.response.sqs.provider}')")
public class DataComplianceResponseQueueConfig {

    @Bean
    @ConditionalOnProperty(name = "data.compliance.response.sqs.provider", havingValue = "aws")
    public AmazonSQS dataComplianceResponseSqsClient(
            @Value("${data.compliance.response.sqs.aws.access.key.id}") final String accessKey,
            @Value("${data.compliance.response.sqs.aws.secret.access.key}") final String secretKey,
            @Value("${data.compliance.response.sqs.region}") final String region) {

        log.info("Creating AWS data compliance response SQS client");

        var credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "data.compliance.response.sqs.provider", havingValue = "aws")
    public AmazonSQS dataComplianceResponseSqsDlqClient(
            @Value("${data.compliance.response.sqs.dlq.aws.access.key.id}") final String accessKey,
            @Value("${data.compliance.response.sqs.dlq.aws.secret.access.key}") final String secretKey,
            @Value("${data.compliance.response.sqs.region}") final String region) {

        log.info("Creating AWS data compliance response SQS client for DLQ");

        var credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean("dataComplianceResponseSqsClient")
    @ConditionalOnProperty(name = "data.compliance.response.sqs.provider", havingValue = "localstack")
    public AmazonSQS sqsClientLocalstack(
            @Value("${data.compliance.response.sqs.endpoint.url}") final String serviceEndpoint,
            @Value("${data.compliance.response.sqs.region}") final String region) {

        log.info("Creating Localstack data compliance response SQS client");

        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region))
                .build();
    }

    @Bean("dataComplianceResponseSqsDlqClient")
    @ConditionalOnProperty(name = "data.compliance.response.sqs.provider", havingValue = "localstack")
    public AmazonSQS sqsDlqClientLocalstack(
            @Value("${data.compliance.response.sqs.endpoint.url}") final String serviceEndpoint,
            @Value("${data.compliance.response.sqs.region}") final String region) {

        log.info("Creating Localstack data compliance response SQS client for DLQ");

        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region))
                .build();
    }
}
