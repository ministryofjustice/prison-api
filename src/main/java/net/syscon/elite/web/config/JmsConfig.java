package net.syscon.elite.web.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

@Slf4j
@EnableJms
@Configuration
@ConditionalOnProperty(name = "offender.deletion.sqs.provider")
public class JmsConfig {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            final AmazonSQS awsSqs,
            @Value("${offender.deletion.sqs.concurrency:3-10}") final String concurrency) {

        final var factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(new SQSConnectionFactory(new ProviderConfiguration(), awsSqs));
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency(concurrency);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(throwable -> log.error("JMS error occurred", throwable));

        return factory;
    }

    @Bean
    @ConditionalOnProperty(name = "offender.deletion.sqs.provider", havingValue = "localstack")
    @Primary
    public AmazonSQSAsync awsSqsClient(@Value("${offender.deletion.sqs.endpoint.url}") final String serviceEndpoint,
                                       @Value("${offender.deletion.sqs.region}") final String region) {

        log.debug("Creating Localstack SQS client");

        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "offender.deletion.sqs.provider", havingValue = "aws")
    @Primary
    public AmazonSQSAsync awsLocalClient(@Value("${offender.deletion.sqs.aws.access.key.id}") final String accessKey,
                                         @Value("${offender.deletion.sqs.aws.secret.access.key}") final String secretKey,
                                         @Value("${offender.deletion.sqs.region}") final String region) {

        log.debug("Creating AWS SQS client");

        var credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }
}
