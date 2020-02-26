package uk.gov.justice.hmpps.nomis.datacompliance.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.sns.provider}')")
public class OffenderDeletionSnsConfig {

    @Bean
    @ConditionalOnProperty(name = "data.compliance.sns.provider", havingValue = "aws")
    public AmazonSNS awsSnsClient(@Value("${data.compliance.sns.aws.access.key.id}") final String accessKeyId,
                                  @Value("${data.compliance.sns.aws.secret.access.key}") final String secretAccessKey,
                                  @Value("${data.compliance.sns.region}") final String region) {

        log.info("Creating SNS Client");

        final var credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);

        return AmazonSNSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean("awsSnsClient")
    @ConditionalOnProperty(name = "data.compliance.sns.provider", havingValue = "localstack", matchIfMissing = true)
    public AmazonSNS awsSnsClientLocalStack(@Value("${data.compliance.sns.endpoint.url}") final String serviceEndpoint,
                                            @Value("${data.compliance.sns.region}") final String region) {

        log.info("Creating LocalStack SNS Client");
        return AmazonSNSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region))
                .build();
    }
}