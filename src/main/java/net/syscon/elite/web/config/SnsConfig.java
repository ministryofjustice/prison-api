package net.syscon.elite.web.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnsConfig {

    @Bean
    @ConditionalOnProperty(name = "offender.deletion.sns.provider", havingValue = "aws")
    AmazonSNS awsSnsClient(@Value("${offender.deletion.sns.aws.access.key.id}") String accessKey,
                           @Value("${offender.deletion.sns.aws.secret.access.key}") String secretKey,
                           @Value("${offender.deletion.sns.endpoint.region}") String region) {
        return AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(region)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "offender.deletion.sns.provider", havingValue = "localstack")
    AmazonSNS awsLocalClient(@Value("${offender.deletion.sns.endpoint.url}") String serviceEndpoint,
                             @Value("${offender.deletion.sns.endpoint.region}") String region) {
        return AmazonSNSClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region))
                .withRegion(region)
                .build();
    }
}
