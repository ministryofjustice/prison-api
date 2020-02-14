package net.syscon.elite.web.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
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
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

@Slf4j
@EnableJms
@Configuration
@ConditionalOnExpression("'${offender.deletion.sqs.provider}'.equals('aws') or '${offender.deletion.sqs.provider}'.equals('localstack')")
public class JmsConfig {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            final AmazonSQS awsSqsClient,
            @Value("${offender.deletion.sqs.concurrency:1}") final String concurrency) {

        final var factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(new SQSConnectionFactory(new ProviderConfiguration(), awsSqsClient));
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency(concurrency);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(throwable -> log.error("JMS error occurred", throwable));

        return factory;
    }

    @Bean
    @ConditionalOnProperty(name = "offender.deletion.sqs.provider", havingValue = "aws")
    public AmazonSQS awsSqsClient(@Value("${offender.deletion.sqs.aws.access.key.id}") final String accessKey,
                                  @Value("${offender.deletion.sqs.aws.secret.access.key}") final String secretKey,
                                  @Value("${offender.deletion.sqs.region}") final String region) {

        log.info("Creating AWS SQS client");

        var credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "offender.deletion.sqs.provider", havingValue = "aws")
    public AmazonSQS awsSqsDlqClient(@Value("${offender.deletion.sqs.dlq.aws.access.key.id}") final String accessKey,
                                     @Value("${offender.deletion.sqs.dlq.aws.secret.access.key}") final String secretKey,
                                     @Value("${offender.deletion.sqs.region}") final String region) {

        log.info("Creating AWS SQS client for DLQ");

        var credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean("awsSqsClient")
    @ConditionalOnProperty(name = "offender.deletion.sqs.provider", havingValue = "localstack")
    public AmazonSQS awsSqsClientLocalstack(@Value("${offender.deletion.sqs.endpoint.url}") final String serviceEndpoint,
                                            @Value("${offender.deletion.sqs.region}") final String region) {

        log.info("Creating Localstack SQS client");

        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region))
                .build();
    }

    @Bean("awsSqsDlqClient")
    @ConditionalOnProperty(name = "offender.deletion.sqs.provider", havingValue = "localstack")
    public AmazonSQS awsSqsDlqClientLocalstack(@Value("${offender.deletion.sqs.endpoint.url}") final String serviceEndpoint,
                                               @Value("${offender.deletion.sqs.region}") final String region) {

        log.info("Creating Localstack SQS client for DLQ");

        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region))
                .build();
    }
}
